from fastapi import APIRouter, Body, HTTPException, Depends
from sqlalchemy.orm import Session
from auth import hash_password, verify_password, create_access_token
from database import get_db
from models import User
from schemas import SignupRequest, LoginRequest, TokenRequest

router = APIRouter()

@router.post("/signup")
def signup(data: SignupRequest, db: Session = Depends(get_db)):
    if db.query(User).filter(User.email == data.email).first():
        raise HTTPException(status_code=400, detail="Email already registered")
    user = User(email=data.email, password=hash_password(data.password))
    db.add(user)
    db.commit()
    db.refresh(user)
    return {"message": "Signup successful"}

@router.post("/login")
def login(data: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == data.email).first()
    if not user or not verify_password(data.password, user.password):
        raise HTTPException(status_code=401, detail="Invalid credentials")
    token = create_access_token({"user_id": user.id})
    return {"access_token": token}

@router.post("/set-username")
def set_username(email: str = Body(...), username: str = Body(...), db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == email).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    user.username = username
    db.commit()
    db.refresh(user)
    return {"message": "Username updated successfully!"}
