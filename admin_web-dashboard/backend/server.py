from dotenv import load_dotenv
from pathlib import Path
load_dotenv(Path(__file__).parent / ".env")

import os
import logging
from datetime import datetime, timezone

from fastapi import FastAPI, APIRouter, HTTPException, Depends
from starlette.middleware.cors import CORSMiddleware
from pydantic import BaseModel

import auth as auth_mod
import firebase_service


app = FastAPI(title="CleanNode API")
api = APIRouter(prefix="/api")

get_current_user, require_admin = auth_mod.make_auth_deps()

DEFAULT_SETTINGS = {
    "_id": "thresholds",
    "pci_clean_min": 75,
    "pci_moderate_min": 45,
    "gas_threshold": 2500,
    "humidity_threshold": 80,
    "overcrowd_threshold": 3,
    "danger_zone_pci": 20,
    "hours_since_cleaned_alert": 12,
    "acceptance_sla_minutes": 15,
}

DEFAULT_STAFF = ["Suresh", "Ramesh", "Lakshmi", "Fatima", "Girish"]

PROOF_PHOTOS = [
    "https://images.unsplash.com/photo-1656646523710-eec180420c2f?crop=entropy&cs=srgb&fm=jpg&q=85&w=800",
    "https://images.unsplash.com/photo-1597444264441-c7acf295d687?crop=entropy&cs=srgb&fm=jpg&q=85&w=800",
    "https://images.unsplash.com/photo-1569597967185-cd6120712154?crop=entropy&cs=srgb&fm=jpg&q=85&w=800",
    "https://images.unsplash.com/photo-1656646523723-b660b32dbaaf?crop=entropy&cs=srgb&fm=jpg&q=85&w=800",
    "https://images.pexels.com/photos/127726/pexels-photo-127726.jpeg?auto=compress&cs=tinysrgb&h=650&w=940",
]

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")
logger = logging.getLogger("cleannode")


def _now_ms() -> int:
    return int(datetime.now(timezone.utc).timestamp() * 1000)


def _demo_proof(did: str) -> str:
    return PROOF_PHOTOS[sum(ord(ch) for ch in did) % len(PROOF_PHOTOS)]


# ---------------- models ----------------
class SettingsBody(BaseModel):
    pci_clean_min: int
    pci_moderate_min: int
    gas_threshold: int
    humidity_threshold: int
    overcrowd_threshold: int
    danger_zone_pci: int
    hours_since_cleaned_alert: int
    acceptance_sla_minutes: int


class DutyUpdate(BaseModel):
    status: str | None = None
    remarks: str | None = None
    staff_response: str | None = None
    staff_name: str | None = None
    verification: str | None = None
    verify_note: str | None = None


class CreateDuty(BaseModel):
    facility_id: str
    block_id: str
    cubicle_id: str
    staff_name: str
    priority: str = "High"
    remarks: str = ""


class StaffCreate(BaseModel):
    name: str
    phone: str = ""


class AlertAck(BaseModel):
    facility_id: str
    cubicle_id: str
    note: str = ""
    resolved: bool = False


# ---------------- snapshot helpers ----------------
async def _snapshot_with_overrides():
    snap = dict(await firebase_service.get_snapshot())
    settings = await firebase_service.get_settings(DEFAULT_SETTINGS)
    sla_ms = int(settings.get("acceptance_sla_minutes", 15)) * 60000
    now = _now_ms()
    assignments = dict(snap.get("assignments", {}))
    for m in await firebase_service.get_collection("assignments_manual"):
        assignments[m["id"]] = m
    overrides = await firebase_service.get_collection("assignment_overrides")
    omap = {o["id"]: o for o in overrides}
    merged = {}
    for did, duty in assignments.items():
        d = dict(duty)
        ov = omap.get(did, {})
        for k in ("status", "remarks", "staff_response", "staff_name", "started_time",
                  "completed_time", "proof_photo", "verification", "verify_note",
                  "reassigned_by", "reassigned_at"):
            if ov.get(k) is not None:
                d[k] = ov[k]
        if d.get("proof_photo") is None and (
            d.get("status") in ("In Progress", "Completed") or sum(ord(ch) for ch in did) % 5 < 2
        ):
            d["proof_photo"] = _demo_proof(did)
        if d.get("proof_photo") and d.get("verification") is None:
            d["verification"] = "Pending"
        # escalation: auto-dispatched duty not accepted within SLA -> admin action needed
        waiting_ms = (now - d["assigned_time"]) if d.get("assigned_time") else 0
        d["minutes_waiting"] = max(0, int(waiting_ms / 60000))
        d["escalated"] = (
            d.get("status") in ("Dispatched", "Pending")
            and (d.get("staff_response") in ("Pending", None))
            and waiting_ms > sla_ms
        )
        merged[did] = d
    snap["assignments"] = merged
    return snap


def _all_cubicles(snap):
    out = []
    for fac in snap["facilities"].values():
        for c in fac["cubicles"].values():
            out.append({"facility": fac, "cubicle": c})
    return out


async def _staff_accounts():
    docs = await firebase_service.get_collection("staff")
    return [{"id": d.get("id"), "name": d.get("name"), "phone": d.get("phone", "")} for d in docs]


# ---------------- auth ----------------
@api.get("/")
async def root():
    return {"message": "CleanNode API online"}


@api.post("/auth/login")
async def login():
    # Password authentication happens only in Firebase on the frontend. This endpoint
    # intentionally does not accept or compare passwords.
    raise HTTPException(status_code=400, detail="Use Firebase Authentication to sign in")


@api.get("/auth/me")
async def me(user: dict = Depends(get_current_user)):
    return user


@api.post("/auth/logout")
async def logout(user: dict = Depends(get_current_user)):
    return {"ok": True}


# ---------------- data ----------------
@api.get("/snapshot")
async def snapshot(user: dict = Depends(get_current_user)):
    snap = await _snapshot_with_overrides()
    settings = await firebase_service.get_settings(DEFAULT_SETTINGS)
    snap["settings"] = {k: v for k, v in settings.items() if k != "_id"}
    return snap


@api.get("/facilities")
async def facilities(user: dict = Depends(get_current_user)):
    snap = await _snapshot_with_overrides()
    return {"facilities": snap["facilities"], "areas": snap["areas"]}


@api.get("/facilities/{facility_id}")
async def facility_detail(facility_id: str, user: dict = Depends(get_current_user)):
    snap = await _snapshot_with_overrides()
    fac = snap["facilities"].get(facility_id)
    if not fac:
        raise HTTPException(status_code=404, detail="Facility not found")
    duties = [d for d in snap["assignments"].values() if d["facility_id"] == facility_id]
    hist = [h for h in snap["history"] if h["facility_id"] == facility_id][:300]
    return {"facility": fac, "assignments": duties, "history": hist}


@api.get("/alerts")
async def alerts(user: dict = Depends(get_current_user)):
    snap = await _snapshot_with_overrides()
    settings = await firebase_service.get_settings(DEFAULT_SETTINGS)
    low_pci = settings.get("pci_moderate_min", 45)
    acks = {(a["facility_id"], a["cubicle_id"]): a for a in await firebase_service.get_collection("alert_acks")}
    items = []
    for row in _all_cubicles(snap):
        c = row["cubicle"]
        if c["alert"] == "None" and c["PCI"] >= low_pci:
            continue
        ack = acks.get((row["facility"]["id"], c["id"]))
        if ack and ack.get("resolved"):
            continue
        items.append({
            "facility_id": row["facility"]["id"],
            "facility_name": row["facility"]["name"],
            "area_name": row["facility"]["area_name"],
            "cubicle": c,
            "ack": ack,
        })
    items.sort(key=lambda x: x["cubicle"]["PCI"])
    return {"alerts": items}


@api.post("/alerts/ack")
async def ack_alert(body: AlertAck, user: dict = Depends(require_admin)):
    key = {"facility_id": body.facility_id, "cubicle_id": body.cubicle_id}
    await firebase_service.upsert("alert_acks", f"{body.facility_id}__{body.cubicle_id}", {**key, "note": body.note, "resolved": body.resolved, "handled": True, "by": user["name"], "at": datetime.now(timezone.utc).isoformat()})
    return {"ok": True}


@api.get("/anomalies")
async def anomalies(user: dict = Depends(get_current_user)):
    snap = await _snapshot_with_overrides()
    items = []
    for row in _all_cubicles(snap):
        ma = row["cubicle"]["ml"]["multisensor_anomaly"]
        if ma["anomaly_detected"]:
            items.append({
                "facility_id": row["facility"]["id"],
                "facility_name": row["facility"]["name"],
                "area_name": row["facility"]["area_name"],
                "cubicle": row["cubicle"],
            })
    items.sort(key=lambda x: x["cubicle"]["ml"]["multisensor_anomaly"]["severity_score"], reverse=True)
    return {"anomalies": items}


@api.get("/assignments")
async def assignments(user: dict = Depends(get_current_user)):
    snap = await _snapshot_with_overrides()
    duties = sorted(snap["assignments"].values(), key=lambda d: d.get("assigned_time") or 0, reverse=True)
    return {"assignments": duties, "staff": await _staff_accounts()}


@api.post("/assignments")
async def create_duty(body: CreateDuty, user: dict = Depends(require_admin)):
    snap = await firebase_service.get_snapshot()
    fac = snap["facilities"].get(body.facility_id)
    if not fac:
        raise HTTPException(status_code=404, detail="Facility not found")
    cub = fac["cubicles"].get(f"{body.block_id}/{body.cubicle_id}")
    did = f"MAN-{_now_ms()}"
    doc = {
        "id": did, "facility_id": body.facility_id, "area_id": fac["area"],
        "block_id": body.block_id, "cubicle_id": body.cubicle_id,
        "staff_name": body.staff_name, "staff_uid": "",
        "staff_response": "Pending", "status": "Dispatched", "priority": body.priority,
        "pci_at_dispatch": cub["PCI"] if cub else 0,
        "alert_at_dispatch": cub["alert"] if cub else "None",
        "assigned_by": user["name"], "assigned_time": _now_ms(),
        "started_time": None, "completed_time": None, "remarks": body.remarks,
        "escalation_log": [], "manual": True,
    }
    await firebase_service.upsert("assignments_manual", did, doc)
    return {"ok": True, "id": did}


@api.patch("/assignments/{duty_id}")
async def update_assignment(duty_id: str, body: DutyUpdate, user: dict = Depends(get_current_user)):
    snap = await _snapshot_with_overrides()
    if duty_id not in snap["assignments"]:
        raise HTTPException(status_code=404, detail="Assignment not found")
    update = {"id": duty_id}
    if body.status is not None:
        update["status"] = body.status
        if body.status == "In Progress":
            update["started_time"] = _now_ms()
        if body.status == "Completed":
            update["completed_time"] = _now_ms()
    if body.remarks is not None:
        update["remarks"] = body.remarks
    if body.staff_response is not None:
        update["staff_response"] = body.staff_response
    if body.staff_name is not None:
        # admin reassigns an escalated (unaccepted) duty to another staffer
        update["staff_name"] = body.staff_name
        update["status"] = "Dispatched"
        update["staff_response"] = "Pending"
        update["started_time"] = None
        update["reassigned_by"] = user["name"]
        update["reassigned_at"] = datetime.now(timezone.utc).isoformat()
    if body.verification is not None:
        update["verification"] = body.verification
    if body.verify_note is not None:
        update["verify_note"] = body.verify_note
    await firebase_service.upsert("assignment_overrides", duty_id, update)
    return {"ok": True, "override": update}


@api.get("/history")
async def history(facility_id: str | None = None, cubicle: str | None = None,
                  user: dict = Depends(get_current_user)):
    snap = await _snapshot_with_overrides()
    rows = snap["history"]
    if facility_id:
        rows = [r for r in rows if r["facility_id"] == facility_id]
    if cubicle:
        rows = [r for r in rows if r["cubicle"] == cubicle]
    return {"history": rows[:500]}


@api.get("/analytics")
async def analytics(user: dict = Depends(get_current_user)):
    snap = await _snapshot_with_overrides()
    hist = snap["history"]
    by_day = {}
    for r in hist:
        day = datetime.fromtimestamp(r["timestamp"] / 1000).strftime("%m-%d") if r["timestamp"] else "?"
        b = by_day.setdefault(day, {"day": day, "pci_sum": 0, "n": 0, "usage": 0})
        b["pci_sum"] += r["PCI"]; b["n"] += 1; b["usage"] += r["occupancy_count"]
    days = [{"day": d, "avg_pci": round(by_day[d]["pci_sum"] / by_day[d]["n"], 1) if by_day[d]["n"] else 0,
             "usage": round(by_day[d]["usage"], 1)} for d in sorted(by_day.keys())]
    by_hour = {h: {"usage": 0} for h in range(24)}
    for r in hist:
        if r["timestamp"]:
            by_hour[datetime.fromtimestamp(r["timestamp"] / 1000).hour]["usage"] += r["occupancy_count"]
    hours = [{"hour": f"{h:02d}:00", "usage": round(by_hour[h]["usage"], 1)} for h in range(24)]
    peak = max(hours, key=lambda x: x["usage"]) if hours else None
    per_facility = []
    for fac in snap["facilities"].values():
        usage = sum(c["ml"]["usage_forecast"]["current_usage"] for c in fac["cubicles"].values())
        predicted = sum(c["ml"]["usage_forecast"]["predicted_usage"] for c in fac["cubicles"].values())
        per_facility.append({"facility": fac["name"], "usage": round(usage, 1), "predicted": round(predicted, 1)})
    return {"days": days, "hours": hours, "peak_hour": peak, "per_facility": per_facility}


@api.get("/heatmap")
async def heatmap(user: dict = Depends(get_current_user)):
    snap = await _snapshot_with_overrides()
    areas = []
    for aid, area in snap["areas"].items():
        facs, pcis = [], []
        for i, fid in enumerate(area["facilities"]):
            fac = snap["facilities"][fid]
            cubs = list(fac["cubicles"].values())
            avg = round(sum(c["PCI"] for c in cubs) / len(cubs), 1) if cubs else 0
            pcis.append(avg)
            facs.append({
                "id": fid, "name": fac["name"], "avg_pci": avg, "cubicles": len(cubs),
                "alerts": sum(1 for c in cubs if c["alert"] != "None"),
                "lat": (area.get("lat") or 12.95) + (i - 1) * 0.004,
                "lng": (area.get("lng") or 77.59) + (i - 1) * 0.004,
            })
        areas.append({"id": aid, "name": area["name"], "lat": area.get("lat"), "lng": area.get("lng"),
                      "avg_pci": round(sum(pcis) / len(pcis), 1) if pcis else 0, "facilities": facs})
    return {"areas": areas}


@api.get("/staff")
async def staff(user: dict = Depends(get_current_user)):
    snap = await _snapshot_with_overrides()
    counts = {}
    for d in snap["assignments"].values():
        c = counts.setdefault(d["staff_name"], {"name": d["staff_name"], "total": 0, "active": 0, "completed": 0})
        c["total"] += 1
        if d["status"] == "Completed":
            c["completed"] += 1
        else:
            c["active"] += 1
    return {"staff_accounts": await _staff_accounts(), "staff_duties": list(counts.values())}


@api.post("/staff")
async def add_staff(body: StaffCreate, user: dict = Depends(require_admin)):
    sid = f"staff-{_now_ms()}"
    await firebase_service.upsert("staff", sid, {"id": sid, "name": body.name, "phone": body.phone})
    return {"ok": True, "id": sid}


@api.delete("/staff/{staff_id}")
async def delete_staff(staff_id: str, user: dict = Depends(require_admin)):
    await firebase_service.delete("staff", staff_id)
    return {"ok": True}


# ---------------- settings ----------------
@api.get("/settings")
async def get_settings(user: dict = Depends(get_current_user)):
    return await firebase_service.get_settings(DEFAULT_SETTINGS)


@api.put("/settings")
async def put_settings(body: SettingsBody, user: dict = Depends(require_admin)):
    doc = body.model_dump()
    await firebase_service.save_settings(doc)
    return doc


app.include_router(api)
app.add_middleware(
    CORSMiddleware,
    allow_credentials=False,
    allow_origins=os.environ.get("CORS_ORIGINS", "*").split(","),
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
async def startup():
    configured = auth_mod.init_firebase_admin()
    if not configured:
        logger.warning("Firebase Admin is not configured; API starts, but authenticated routes will return 503 until configured")
    await firebase_service.ensure_defaults(DEFAULT_SETTINGS, DEFAULT_STAFF)
    logger.info("CleanNode API startup complete")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("server:app", host="0.0.0.0", port=int(os.getenv("PORT", "8000")), reload=False)
