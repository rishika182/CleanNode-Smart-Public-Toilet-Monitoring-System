"""Firebase Authentication verification for CleanNode.
The backend never stores or checks passwords."""
import os
from typing import Any
import firebase_admin
from firebase_admin import auth as firebase_auth, credentials
from fastapi import HTTPException, Request

_ready = False
_error = None

def init_firebase_admin() -> bool:
    global _ready, _error
    if firebase_admin._apps:
        _ready = True; return True
    try:
        path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        if path:
            cred = credentials.Certificate(path)
        else:
            cred = credentials.ApplicationDefault()
        opts = {"projectId": os.getenv("FIREBASE_PROJECT_ID")} if os.getenv("FIREBASE_PROJECT_ID") else None
        firebase_admin.initialize_app(cred, opts)
        _ready = True; _error = None
    except Exception as exc:
        _ready = False; _error = str(exc)
    return _ready

def _token(request: Request):
    h = request.headers.get("Authorization", "")
    return h[7:] if h.startswith("Bearer ") else None

def _role(decoded: dict[str, Any]):
    role = decoded.get("role") or decoded.get("cleanNodeRole") or "user"
    return role if role in {"admin", "municipal_authority", "staff", "maintenance", "user"} else "user"

def make_auth_deps():
    async def get_current_user(request: Request):
        if not _ready and not init_firebase_admin():
            raise HTTPException(503, "Firebase Admin credentials are not configured on the backend")
        token = _token(request)
        if not token: raise HTTPException(401, "Not authenticated")
        try: decoded = firebase_auth.verify_id_token(token, check_revoked=True)
        except Exception: raise HTTPException(401, "Invalid or expired authentication token")
        return {"id": decoded["uid"], "email": decoded.get("email", ""),
                "name": decoded.get("name") or decoded.get("email", "CleanNode User").split("@")[0],
                "role": _role(decoded)}
    async def require_admin(request: Request):
        u = await get_current_user(request)
        if u["role"] not in {"admin", "municipal_authority"}: raise HTTPException(403, "Admin access required")
        return u
    return get_current_user, require_admin
