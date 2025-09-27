from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from database import get_db
import models, schemas
import uuid
import bcrypt
from utils.session_check import get_current_user
from .product_routes import llm_model,llm_tokenizer,start_token,end_token

router = APIRouter(prefix="", tags=["Assistant"])

def assistant(role ,task):
    # LLM 모델 프롬프트 설정
    messages = [
      {"role": "system", "content": role},
      {"role": "user", "content": task}
  ]

    inputs = llm_tokenizer.apply_chat_template(messages, return_tensors="pt").to(llm_model.device)


    eos_token_id = [
        llm_tokenizer.eos_token_id,
        llm_tokenizer.convert_tokens_to_ids("<|eot_id|>")
  ]

    outputs = llm_model.generate(
        inputs,
        eos_token_id=eos_token_id,
        max_length=8192
  )
    # AI output 전처리

    result = (llm_tokenizer.decode(outputs[0]))

    start_index = result.find(start_token) + len(start_token)
    start_index = result.find(start_token, start_index) + len(start_token)
    start_index = result.find(start_token ,start_index)+ len(start_token)

    end_index = result.find(end_token) + len(end_token)
    end_index = result.find(end_token,end_index) + len(end_token)
    end_index = result.find(end_token, end_index)

    result = result[start_index+2:end_index]
    result.replace('\n','')

    return result


# Check Email
@router.post("/AssistantCategory",response_model = dict)
def assistant_category(payload: schemas.AssistantCategory, db: Session = Depends(get_db)):
	# payload.email
	print("debug")	
	role = "너는 사용자의 말을 카테고리에 맞게 분류해야돼."
	task = f"<|사용자 질문|> {payload.voiceInput} <|사용자 질문|>  사용자 질문을 참고해서 주어진 질문이 다음 중 무엇에 해당하는지 출력해줘. 이름만 사용할거고 다른 내용이 덧붙여지면 오류가 발생하니 이름만 출력해줘. 사용할 수 있는 이름은 다음에 제시될 단어 안에서 선택해줘 <|단어|>상품검색,계정설정,카테고리,장바구니,인기상품,할인상품,배송지 변경,결제수단 변경, 도움말<|단어|>"

	result = assistant(role, task)
	
	return {"category" : result}


