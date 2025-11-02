from fastapi import Request, HTTPException, Depends
from fastapi import Query
from sqlalchemy.orm import Session
from database import get_db
import models

def get_current_user(
    request: Request, 
    db: Session = Depends(get_db),
    session_id: str = Query(default=None)
):
    session_id = session_id or request.headers.get("session_id")
    if not session_id:
        raise HTTPException(status_code=401, detail="Unauthorized")

    user = db.query(models.User).filter_by(session_id=session_id).first()
    if not user:
        raise HTTPException(status_code=401, detail="Invalid session")

    return user

def check_session(db,session_id = None):
    if not session_id:
        raise HTTPException(status_code=401, detail="Unauthorized")

    session = db.query(models.Session).filter_by(session_id=session_id).first()
    user = db.query(models.User).filter(models.User.id == session.user_id).first()
    if not user:
        raise HTTPException(status_code=401, detail="Invalid session")

    return user

