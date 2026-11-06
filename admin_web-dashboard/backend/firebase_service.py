"""Reads the CleanNode Firebase Realtime Database over REST and normalizes it
into the snapshot shape the dashboard consumes. Falls back to a realistic
simulator when Firebase is unreachable or empty."""
import os
import time
import asyncio
from datetime import datetime, timezone

import requests

import simulator

FIREBASE_URL = os.environ.get("FIREBASE_DB_URL", "").rstrip("/")
FIREBASE_SECRET = os.environ.get("FIREBASE_DB_SECRET", "") or None

# Bengaluru area metadata (names + map coordinates for the heatmap - FR11).
AREA_META = {
    "Jayanagar": {"name": "Jayanagar", "lat": 12.9250, "lng": 77.5938},
    "Cubbon_Park": {"name": "Cubbon Park", "lat": 12.9763, "lng": 77.5929},
}

_cache = {"data": None, "ts": 0.0}
_CACHE_TTL = 6.0  # seconds


def _auth_params():
    return {"auth": FIREBASE_SECRET} if FIREBASE_SECRET else {}


def _get(path: str, params=None):
    url = f"{FIREBASE_URL}/{path}.json"
    p = _auth_params()
    if params:
        p.update(params)
    r = requests.get(url, params=p, timeout=8)
    if r.status_code != 200:
        raise RuntimeError(f"Firebase GET {path} -> {r.status_code}")
    return r.json()


def _to_ms(ts):
    """ISO string -> epoch ms."""
    if ts is None:
        return None
    if isinstance(ts, (int, float)):
        return int(ts)
    s = str(ts).strip().replace(" ", "T", 1) if " " in str(ts) and "T" not in str(ts) else str(ts)
    for fmt in None, "iso":
        pass
    try:
        return int(datetime.fromisoformat(s).timestamp() * 1000)
    except Exception:
        try:
            return int(datetime.strptime(s[:19], "%Y-%m-%dT%H:%M:%S").timestamp() * 1000)
        except Exception:
            return None


def _num(v, default=0.0):
    try:
        return float(v)
    except (TypeError, ValueError):
        return float(default)


def _pretty_name(fid: str) -> str:
    return fid.replace("_BBMP_", " BBMP-").replace("_", " ")


def _norm_alert(alert):
    a = (alert or "").strip()
    if a in ("", "Normal", "None"):
        return "None"
    return a


def _norm_ml(ml: dict) -> dict:
    ml = ml or {}
    pm = ml.get("predictive_maintenance", {}) or {}
    uf = ml.get("usage_forecast", {}) or {}
    ma = ml.get("multisensor_anomaly", {}) or {}
    return {
        "predictive_maintenance": {
            "risk": pm.get("risk", "Low"),
            "risk_score": _num(pm.get("risk_score", 0)),
            "reasons": pm.get("reasons", []) or [],
            "updated": _to_ms(pm.get("updated_at")) or 0,
        },
        "usage_forecast": {
            "current_usage": _num(uf.get("current_usage", 0)),
            "predicted_usage": _num(uf.get("predicted_usage", 0)),
            "trend": uf.get("trend", "stable"),
            "confidence": _num(uf.get("confidence", 0)),
            "historical_samples": int(_num(uf.get("historical_samples", 0))),
            "updated": _to_ms(uf.get("updated_at")) or 0,
        },
        "multisensor_anomaly": {
            "anomaly_detected": bool(ma.get("anomaly_detected", False)),
            "severity": ma.get("severity", "None"),
            "severity_score": _num(ma.get("severity_score", 0)),
            "anomalies": ma.get("anomalies", []) or [],
            "sensors_checked": ma.get("sensors_checked", []) or [],
            "updated": _to_ms(ma.get("updated_at")) or 0,
        },
    }


def _norm_cubicle(block_id: str, cid: str, raw: dict) -> dict:
    occ_count = _num(raw.get("occupancy_count", raw.get("occupancy", 0)))
    return {
        "id": f"{block_id}/{cid}",
        "block": block_id,
        "cubicle": cid,
        "temperature": round(_num(raw.get("temperature")), 1),
        "humidity": round(_num(raw.get("humidity")), 1),
        "gas": round(_num(raw.get("gas")), 1),
        "occupancy": occ_count > 0,
        "occupancy_count": occ_count,
        "water_status": raw.get("water_status", "OK"),
        "hours_since_cleaned": round(_num(raw.get("hours_since_cleaned")), 1),
        "PCI": round(_num(raw.get("PCI")), 1),
        "alert": _norm_alert(raw.get("alert")),
        "ml": _norm_ml(raw.get("ml")),
        "last_updated": _to_ms(raw.get("last_updated") or raw.get("timestamp")) or 0,
    }


def _build_from_firebase() -> dict:
    facilities_raw = _get("facilities") or {}
    if not facilities_raw:
        raise RuntimeError("Firebase facilities empty")

    facilities = {}
    areas = {}
    for fid, fac in facilities_raw.items():
        blocks = (fac or {}).get("blocks", {}) or {}
        cubicles = {}
        area_id = None
        block_names = []
        for bid, block in blocks.items():
            block_names.append(bid)
            for cid, raw in (block.get("cubicles", {}) or {}).items():
                nc = _norm_cubicle(bid, cid, raw)
                cubicles[nc["id"]] = nc
                area_id = area_id or raw.get("area_id")
        area_id = area_id or fid.split("_BBMP_")[0]
        meta = AREA_META.get(area_id, {"name": area_id.replace("_", " ")})
        facilities[fid] = {
            "id": fid,
            "name": _pretty_name(fid),
            "area": area_id,
            "area_name": meta["name"],
            "blocks": sorted(block_names),
            "cubicles": cubicles,
        }
        areas.setdefault(area_id, {
            "id": area_id,
            "name": meta["name"],
            "lat": meta.get("lat"),
            "lng": meta.get("lng"),
            "facilities": [],
        })
        areas[area_id]["facilities"].append(fid)

    # Assignments
    assignments = {}
    for did, duty in (_get("assignments") or {}).items():
        if not isinstance(duty, dict):
            continue
        assignments[did] = {
            "id": did,
            "facility_id": duty.get("facility_id", ""),
            "area_id": duty.get("area_id", ""),
            "block_id": duty.get("block_id", ""),
            "cubicle_id": duty.get("cubicle_id", ""),
            "staff_name": duty.get("staff", "Unassigned"),
            "staff_uid": duty.get("staff_uid", ""),
            "staff_response": duty.get("staff_response", "Pending"),
            "status": duty.get("status", "Dispatched"),
            "priority": duty.get("priority", "Medium"),
            "pci_at_dispatch": _num(duty.get("pci_at_dispatch", 0)),
            "alert_at_dispatch": _norm_alert(duty.get("alert_at_dispatch")),
            "assigned_by": duty.get("assigned_by", ""),
            "assigned_time": _to_ms(duty.get("assigned_time")),
            "started_time": _to_ms(duty.get("started_time")) if duty.get("started_time") else None,
            "completed_time": _to_ms(duty.get("completed_time")) if duty.get("completed_time") else None,
            "remarks": duty.get("remarks", ""),
            "escalation_log": duty.get("escalation_log", []),
        }

    history = _build_history()

    return {
        "source": "firebase",
        "last_sync": int(time.time() * 1000),
        "areas": areas,
        "facilities": facilities,
        "assignments": assignments,
        "history": history,
    }


def _build_history(max_samples: int = 900) -> list:
    """Flatten history/{date}/{area}/{facility}/{block}/{cubicle}/{time} -> list."""
    out = []
    try:
        dates = _get("history", params={"shallow": "true"}) or {}
    except Exception:
        return out
    for date in sorted(dates.keys())[-3:]:
        try:
            tree = _get(f"history/{date}") or {}
        except Exception:
            continue
        for area_id, facs in (tree or {}).items():
            for fid, blocks in (facs or {}).items():
                for bid, cubs in (blocks or {}).items():
                    for cid, samples in (cubs or {}).items():
                        for _tk, pkt in (samples or {}).items():
                            if not isinstance(pkt, dict):
                                continue
                            occ = _num(pkt.get("occupancy_count", pkt.get("occupancy", 0)))
                            out.append({
                                "timestamp": _to_ms(pkt.get("timestamp") or pkt.get("last_updated")) or 0,
                                "area_id": area_id,
                                "facility_id": fid,
                                "block": bid,
                                "cubicle": cid,
                                "PCI": round(_num(pkt.get("PCI")), 1),
                                "gas": round(_num(pkt.get("gas")), 1),
                                "humidity": round(_num(pkt.get("humidity")), 1),
                                "temperature": round(_num(pkt.get("temperature")), 1),
                                "occupancy": occ > 0,
                                "occupancy_count": occ,
                                "water_status": pkt.get("water_status", "OK"),
                                "alert": _norm_alert(pkt.get("alert")),
                            })
    out.sort(key=lambda x: x["timestamp"], reverse=True)
    return out[:max_samples]


async def get_snapshot(force: bool = False) -> dict:
    now = time.time()
    if not force and _cache["data"] and (now - _cache["ts"] < _CACHE_TTL):
        return _cache["data"]
    data = None
    if FIREBASE_URL:
        try:
            data = await asyncio.to_thread(_build_from_firebase)
        except Exception as e:
            print(f"[firebase] falling back to simulator: {e}")
            data = None
    if data is None:
        data = simulator.build_snapshot()
    _cache["data"] = data
    _cache["ts"] = now
    return data

# Application metadata persistence. Firebase is optional for demo mode: when the
# database is unavailable these writes remain in-process and the snapshot reports DEMO.
_memory = {"settings": {}, "staff": {}, "assignments_manual": {}, "assignment_overrides": {}, "alert_acks": {}}

def _write(path, value):
    if not FIREBASE_URL: raise RuntimeError("Firebase URL is not configured")
    r = requests.put(f"{FIREBASE_URL}/{path}.json", params=_auth_params(), json=value, timeout=8)
    if r.status_code not in (200,204): raise RuntimeError(f"Firebase PUT {path} -> {r.status_code}")

def _delete(path):
    if not FIREBASE_URL: raise RuntimeError("Firebase URL is not configured")
    r = requests.delete(f"{FIREBASE_URL}/{path}.json", params=_auth_params(), timeout=8)
    if r.status_code not in (200,204): raise RuntimeError(f"Firebase DELETE {path} -> {r.status_code}")

async def get_collection(name):
    try:
        if FIREBASE_URL:
            raw = await asyncio.to_thread(_get, name)
            if isinstance(raw, dict): return [dict(v, id=v.get("id", k)) if isinstance(v, dict) else {"id": k} for k,v in raw.items()]
    except Exception: pass
    return list(_memory.get(name, {}).values())

async def upsert(name, key, value):
    doc = dict(value); doc.setdefault("id", key); _memory.setdefault(name,{})[key] = doc
    if FIREBASE_URL:
        try: await asyncio.to_thread(_write, f"{name}/{key}", doc)
        except Exception: pass

async def delete(name, key):
    _memory.setdefault(name,{}).pop(key,None)
    if FIREBASE_URL:
        try: await asyncio.to_thread(_delete, f"{name}/{key}")
        except Exception: pass

async def get_settings(default):
    merged = {k:v for k,v in default.items() if k != "_id"}
    try:
        if FIREBASE_URL:
            raw = await asyncio.to_thread(_get, "settings/thresholds")
            if isinstance(raw,dict): merged.update(raw)
    except Exception: pass
    merged.update(_memory.get("settings",{}))
    return merged

async def save_settings(doc):
    _memory["settings"] = dict(doc)
    if FIREBASE_URL:
        try: await asyncio.to_thread(_write, "settings/thresholds", doc)
        except Exception: pass

async def ensure_defaults(settings, staff):
    current = await get_collection("staff")
    if not current:
        for i,name in enumerate(staff,1): await upsert("staff", f"staff-{i}", {"id":f"staff-{i}","name":name,"phone":""})
