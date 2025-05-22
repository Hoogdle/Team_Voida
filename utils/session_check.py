from fastapi import Depends, HTTPException
from sqlalchemy.orm import Session
from database import get_db
import models

def get_current_user(session_id: str = "", db: Session = Depends(get_db)):
    user = db.query(models.UserProfile).filter_by(session_id=session_id).first()
    if not user:
        raise HTTPException(status_code=401, detail="Invalid session")
    return user
