"""Realistic live-data simulator used as an automatic fallback when the
Firebase Realtime Database is unreachable or empty. Mirrors gateway.py's
PCI / alert / ML logic so the dashboard is never empty in preview."""
import time
import random
from datetime import datetime, timedelta

AREA_META = {
    "Jayanagar": {"name": "Jayanagar", "lat": 12.9250, "lng": 77.5938},
    "Cubbon_Park": {"name": "Cubbon Park", "lat": 12.9763, "lng": 77.5929},
}

FACILITY_DEFS = [
    ("Jayanagar_BBMP_01", "Jayanagar", ["Mens", "Womens"]),
    ("Jayanagar_BBMP_02", "Jayanagar", ["Mens", "Womens"]),
    ("Jayanagar_BBMP_03", "Jayanagar", ["Mens", "Womens"]),
    ("Cubbon_Park_BBMP_01", "Cubbon_Park", ["Mens"]),
]
CUBICLES = ["Cubicle_1", "Cubicle_2", "Cubicle_3"]
STAFF = ["Suresh", "Ramesh", "Lakshmi", "Fatima", "Girish"]


def _clamp(v, lo, hi):
    return max(lo, min(hi, v))


def _alert(pci, gas, water, occ):
    if occ >= 1 and water == 0:
        return "Flush Issue"
    if gas > 2500:
        return "Bad Smell"
    if pci < 55:
        return "Needs Cleaning"
    if occ > 3:
        return "Overcrowded"
    return "None"


def _ml(pci, gas, humidity, occ, water, hours):
    # predictive maintenance
    score, reasons = 0, []
    if pci <= 20:
        score += 60; reasons.append("Very low cleanliness index")
    elif pci <= 40:
        score += 40; reasons.append("Low cleanliness index")
    elif pci <= 60:
        score += 20; reasons.append("Moderate cleanliness index")
    if gas > 2500:
        score += 25; reasons.append("High gas/ammonia level")
    if humidity > 80:
        score += 15; reasons.append("High humidity")
    if hours > 12:
        score += 15; reasons.append("Long time since cleaning")
    score = min(score, 100)
    risk = "High" if score >= 70 else "Medium" if score >= 40 else "Low"
    if not reasons:
        reasons = ["No significant maintenance risk detected"]

    # anomaly
    anomalies, sev = [], 0
    if gas > 2500:
        anomalies.append("High gas/ammonia level"); sev += 30
    if humidity > 80:
        anomalies.append("Abnormally high humidity"); sev += 15
    if occ > 3:
        anomalies.append("High occupancy"); sev += 20
    if occ > 0 and water == 0:
        anomalies.append("Occupancy detected but no water flow"); sev += 30
    if pci < 55:
        anomalies.append("Low cleanliness index"); sev += 20
    sev = min(sev, 100)
    severity = "Critical" if sev >= 70 else "Moderate" if sev >= 40 else "Minor" if sev > 0 else "None"

    cur = round(occ + random.random() * 4, 2)
    return {
        "predictive_maintenance": {"risk": risk, "risk_score": score, "reasons": reasons, "updated": int(time.time() * 1000)},
        "usage_forecast": {
            "current_usage": cur,
            "predicted_usage": round(cur * (0.85 + random.random() * 0.4), 2),
            "trend": random.choice(["rising", "stable", "falling"]),
            "confidence": random.choice([40, 55, 70, 80, 90]),
            "historical_samples": random.randint(0, 240),
            "updated": int(time.time() * 1000),
        },
        "multisensor_anomaly": {
            "anomaly_detected": len(anomalies) > 0,
            "severity": severity,
            "severity_score": sev,
            "anomalies": anomalies,
            "sensors_checked": ["temperature", "humidity", "gas", "occupancy", "water_flow", "PCI"],
            "updated": int(time.time() * 1000),
        },
    }


def _cubicle(seed_key, bid, cid):
    rnd = random.Random(hash(seed_key) & 0xFFFFFFFF)
    tickshift = int(time.time() // 8)
    rnd.seed((hash(seed_key) + tickshift) & 0xFFFFFFFF)
    hours = round(rnd.random() * 11, 1)
    gas = round(200 + rnd.random() * 2800, 1)
    humidity = round(45 + rnd.random() * 40, 1)
    temperature = round(23 + rnd.random() * 10, 1)
    water = 0 if rnd.random() > 0.9 else 1
    occ = rnd.choice([0, 0, 0, 1, 1, 2, 4])
    pci = _clamp(round(100 - hours * 4 - gas * 0.005 - humidity * 0.15 - occ * 5 - (15 if (occ > 0 and water == 0) else 0), 1), 2, 99)
    water_status = "Flushed" if water > 0 else "Not Flushed"
    now = int(time.time() * 1000) - int(rnd.random() * 4 * 60000)
    return {
        "id": f"{bid}/{cid}",
        "block": bid,
        "cubicle": cid,
        "temperature": temperature,
        "humidity": humidity,
        "gas": gas,
        "occupancy": occ > 0,
        "occupancy_count": occ,
        "water_status": water_status,
        "hours_since_cleaned": hours,
        "PCI": pci,
        "alert": _alert(pci, gas, water, occ),
        "ml": _ml(pci, gas, humidity, occ, water, hours),
        "last_updated": now,
    }


def build_snapshot():
    facilities, areas = {}, {}
    for fid, area_id, blocks in FACILITY_DEFS:
        cubicles = {}
        for bid in blocks:
            for cid in CUBICLES:
                c = _cubicle(fid + bid + cid, bid, cid)
                cubicles[c["id"]] = c
        meta = AREA_META[area_id]
        facilities[fid] = {
            "id": fid, "name": fid.replace("_BBMP_", " BBMP-").replace("_", " "),
            "area": area_id, "area_name": meta["name"], "blocks": blocks, "cubicles": cubicles,
        }
        areas.setdefault(area_id, {"id": area_id, "name": meta["name"], "lat": meta["lat"], "lng": meta["lng"], "facilities": []})
        areas[area_id]["facilities"].append(fid)

    assignments = {}
    i = 0
    for fac in facilities.values():
        for c in fac["cubicles"].values():
            if c["alert"] == "None" and c["PCI"] > 62:
                continue
            i += 1
            did = str(1786987000000 + i * 1000)
            status = random.choice(["Dispatched", "Accepted", "In Progress", "Completed", "Pending"])
            assigned = int(time.time() * 1000) - random.randint(0, 5) * 3600000
            assignments[did] = {
                "id": did, "facility_id": fac["id"], "area_id": fac["area"],
                "block_id": c["block"], "cubicle_id": c["cubicle"],
                "staff_name": STAFF[i % len(STAFF)], "staff_uid": f"sim{i}",
                "staff_response": "Pending" if status in ("Pending", "Dispatched") else "Acknowledged",
                "status": status,
                "priority": "Danger Zone" if c["PCI"] < 20 else "Critical" if c["PCI"] < 45 else "High" if c["PCI"] < 60 else "Medium",
                "pci_at_dispatch": c["PCI"], "alert_at_dispatch": c["alert"],
                "assigned_by": "System (Danger Zone)", "assigned_time": assigned,
                "started_time": None if status in ("Pending", "Dispatched") else assigned + 900000,
                "completed_time": assigned + 2700000 if status == "Completed" else None,
                "remarks": "", "escalation_log": [],
            }

    history = []
    now = datetime.now()
    for d in range(2, -1, -1):
        for fac in facilities.values():
            for c in fac["cubicles"].values():
                for h in range(0, 24, 3):
                    ts = (now - timedelta(days=d)).replace(hour=h, minute=0, second=0, microsecond=0)
                    rnd = random.Random(hash(fac["id"] + c["id"] + str(d) + str(h)) & 0xFFFFFFFF)
                    gas = round(200 + rnd.random() * 2600, 1)
                    pci = _clamp(round(96 - (h / 24) * 40 - rnd.random() * 22, 1), 5, 99)
                    occ = rnd.choice([0, 0, 1, 2, 3])
                    water = "Flushed" if rnd.random() > 0.1 else "Not Flushed"
                    history.append({
                        "timestamp": int(ts.timestamp() * 1000), "area_id": fac["area"],
                        "facility_id": fac["id"], "block": c["block"], "cubicle": c["cubicle"],
                        "PCI": pci, "gas": gas, "humidity": round(45 + rnd.random() * 35, 1),
                        "temperature": round(23 + rnd.random() * 9, 1), "occupancy": occ > 0,
                        "occupancy_count": occ, "water_status": water,
                        "alert": _alert(pci, gas, 1 if water == "Flushed" else 0, occ),
                    })
    history.sort(key=lambda x: x["timestamp"], reverse=True)

    return {
        "source": "demo", "last_sync": int(time.time() * 1000),
        "areas": areas, "facilities": facilities,
        "assignments": assignments, "history": history[:900],
    }
