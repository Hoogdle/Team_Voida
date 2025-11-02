from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from database import get_db
import models, schemas
import uuid
import bcrypt
from utils.session_check import get_current_user

router = APIRouter(prefix="", tags=["User"])


# Check Email
@router.post("/EmailCheck",response_model = bool)
def email_check(payload: schemas.EmailRequest, db: Session = Depends(get_db)):
	if db.query(models.User).filter(models.User.email == payload.email).first():
	    return False 

	return	True

# Sign up API
@router.post("/SignUp", response_model=dict)
def sign_up(payload: schemas.SignUpRequest, db: Session = Depends(get_db)):
    if db.query(models.User).filter(models.User.un == payload.un).first():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="UserName already registered")

    session_id =  str(uuid.uuid4())
    hashed_pw = bcrypt.hashpw(payload.pw.encode("utf-8"), bcrypt.gensalt())

    new_user = models.User(
        email=payload.email,
        pw=hashed_pw.decode("utf-8"),
        cell=payload.cell,
	    un=payload.un,
        address=""
    )
    db.add(new_user)
    db.commit()

    user = db.query(models.User).filter(models.User.email == payload.email).first()
    
    new_session = models.Session(
        session_id = session_id,
        user_id = user.id,
    )
    db.add(new_session)
    db.commit()

    return {"result" : True, "session_id" : session_id}


# Login API
@router.post("/SignIn", response_model=schemas.LoginResponse)
def sign_in(payload: schemas.LoginRequest, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.email == payload.email).first()
    if not user or not bcrypt.checkpw(payload.pw.encode("utf-8"), user.password.encode("utf-8")):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid email or password")

    session_id = str(uuid.uuid4())

    session = db.query(models.Session).filter(models.Session.user_id == user.id).first()
    session.session_id = session_id
    db.commit()

    return schemas.LoginResponse(session_id=session_id)


# Set username API
@router.post("/UserName", response_model=schemas.LoginResponse)
def set_user_name(
        payload: schemas.UserNameRequest,
        user: models.User = Depends(get_current_user),
        db: Session = Depends(get_db)
):
    if db.query(models.User).filter(models.User.un == payload.un).first():
        return False

    usession_id = str(uuid.uuid4())
    user.session_id = session_id
    user.un = payload.un
    db.commit()

    return schemas.LoginResponse(session_id=session_id)
