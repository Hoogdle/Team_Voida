from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from jose import jwt, JWTError
from models import Review
from database import get_db
from schemas import ReviewCreate
from auth import JWT_SECRET

router = APIRouter()

@router.post("/review")
def create_review(review: ReviewCreate, db: Session = Depends(get_db)):
    try:
        payload = jwt.decode(review.token, JWT_SECRET, algorithms=["HS256"])
        user_id = payload["user_id"]
    except JWTError:
        raise HTTPException(status_code=401, detail="Invalid token")

    db_review = Review(
        user_id=user_id,
        product_id=review.product_id,
        rating=review.rating,
        comment=review.comment,
    )
    db.add(db_review)
    db.commit()
    return {"message": "Review submitted"}
