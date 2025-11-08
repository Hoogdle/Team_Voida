from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from database import get_db
import models, schemas
import uuid
import bcrypt
from utils.session_check import get_current_user
from utils.session_check import *

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
    )

    db.add(new_user)
    db.commit()

    user = db.query(models.User).filter(models.User.email == payload.email).first()

    new_address = models.Address(
        user_id = user.id,
        address = payload.address,
        flag = True
    )	

    db.add(new_address)
    db.commit()
  
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
    if not user or not bcrypt.checkpw(payload.pw.encode("utf-8"), user.pw.encode("utf-8")):
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



@router.post("/ResetPW1", response_model=schemas.ResetStep1Response)
def set_user_name(
        payload: schemas.ResetRequestStep1,
        db: Session = Depends(get_db)
):
    user = db.query(models.User).filter(models.User.email == payload.email, models.User.cell == payload.cell).first()
    if user:
        return schemas.ResetStep1Response(
                    is_user = True,
                    user_id = user.id
                )
    else:
        return schemas.ResetStep1Response(
                    is_user = False,
                    user_id = 0
                )

@router.post("/ResetPW2", response_model=bool)
def set_user_name(
        payload: schemas.ResetStep2Response,
        db: Session = Depends(get_db)
):
    user = db.query(models.User).filter(models.User.id == payload.user_id).first()

    if not user:
        return False
    
    hashed_pw = bcrypt.hashpw(payload.pw.encode("utf-8"), bcrypt.gensalt())
    new_pw = hashed_pw.decode("utf-8")

    user.pw = new_pw
    db.commit()

    if user:
        return True



# Address List
@router.post("/AddressList", response_model=list[schemas.Address])
def set_user_name(
        payload: schemas.RequestWithSession,
        db: Session = Depends(get_db)
):

	user = check_session(db,payload.session_id)
	address_list = db.query(models.Address).filter(models.Address.user_id == user.id).all()
	
	for index, item in enumerate(address_list):
		if item.flag:
			address_list[0], address_list[index] = address_list[index], address_list[0]
		
	return [
		schemas.Address(
			address_id = item.id,
			address_text = item.address,
			flag = item.flag
		)
		for item in address_list
	]	
