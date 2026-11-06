"""Smoke tests for Mongo-free CleanNode backend helpers."""
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import firebase_service

def test_demo_snapshot_has_hierarchy():
    import asyncio
    snap=asyncio.run(firebase_service.get_snapshot(force=True))
    assert snap["areas"] and snap["facilities"]
    assert any(f.get("cubicles") for f in snap["facilities"].values())
