"""Optional MQTT gateway: validates location-aware Wokwi payloads, computes PCI,
and writes cubicle readings to Firebase. React never connects to MQTT."""
import json, os
from datetime import datetime, timezone
import firebase_service

MODEL_PATH = os.getenv("CLEANNODE_MODEL_PATH", "cleanliness_model.joblib")

def _fallback(payload):
    # Explicit deterministic demonstration fallback when no trained model is available.
    score = 100 - (float(payload.get("ammonia_level",0))/30) - max(0,float(payload.get("humidity",0))-60)*0.7 - float(payload.get("occupancy_count",0))*5 - float(payload.get("hours_since_cleaned",0))*2
    return max(0, min(100, round(score,1))), "fallback"

def process_payload(payload: dict):
    required=("area_id","facility_id","block_id","cubicle_id")
    missing=[k for k in required if not payload.get(k)]
    if missing: raise ValueError("Missing location fields: "+", ".join(missing))
    pci, source = _fallback(payload)
    return {**payload, "PCI": pci, "pci_source": source, "timestamp": datetime.now(timezone.utc).isoformat()}

def on_message(_client,_userdata,msg):
    payload=process_payload(json.loads(msg.payload.decode()))
    # Store raw logical hierarchy; firebase_service mapping exposes it to dashboard.
    path=f"sensor_readings/{payload['area_id']}/{payload['facility_id']}/{payload['block_id']}/{payload['cubicle_id']}/latest"
    firebase_service._write(path,payload)

def run():
    import paho.mqtt.client as mqtt
    c=mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    c.on_message=on_message
    c.connect(os.getenv("MQTT_BROKER","broker.hivemq.com"),int(os.getenv("MQTT_PORT","1883")),60)
    c.subscribe(os.getenv("MQTT_TOPIC","pes/cleannode/iot"))
    c.loop_forever()

if __name__ == '__main__': run()
