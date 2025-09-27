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


    input_ids = llm_tokenizer.apply_chat_template(
    messages,
    tokenize=True,
    add_generation_prompt=True,
    return_tensors="pt"
    )

    output = llm_model.generate(
        input_ids.to("cuda"),
        eos_token_id=llm_tokenizer.eos_token_id,
        max_new_tokens=128,
        do_sample=False,
    )
	 
	# AI output 전처리
    result = (llm_tokenizer.decode(output[0]))

    start_index = result.find(start_token) + len(start_token)

    end_index = result.find(end_token) + len(end_token)
    end_index = result.find(end_token, end_index)

    result = result[start_index:end_index]
    result.replace('\n','')

    return result


@router.post("/AssistantCategory",response_model = dict)
def assistant_category(payload: schemas.AssistantCategory, db: Session = Depends(get_db)):
	role = "너는 사용자의 말을 카테고리에 맞게 분류해야돼."
	task = f'"{payload.voiceInput}" 이렇게 사용자 질문이 주어졌을 때, 이 질문이 다음의 "상품검색,계정설정,카테고리,장바구니,인기상품,할인상품,배송지 변경,결제수단 변경, 도움말, 해당없음" 중 어느 곳에 속하는지 알려줘. 이때, 어느 것에 속한다고 그 이유는 출력할 필요는 없고, 그냥 속하는 부분의 명칭만 알려주면 돼."'

	result = assistant(role, task)
	
	return {"category" : result}


@router.post("/AssistantSearch",response_model = dict)
def assistant_category(payload: schemas.AssistantSearch, db: Session = Depends(get_db)):
	role = "너는 사용자의 입력에서 핵심 검색 키워드를 찾아야돼."
	task = f"{payload.voiceInput} 이렇게 사용자 질문이 주어졌을 때, 이를 검색과 관련된 문장으로 간주하고, 해당 내용에서 검색과 관련된 핵심 키워드만을 출력해줘. 왜 그렇게 생각했는지는 출력할 필요 없고 핵심 키워드만 알려주면돼. 절대 다른 문장을 추가하지 말고, 핵심 키워드만 추출해서 그것만 출력해줘. 핵심 키워드는 상품과 관련된 것으로 즉, 상품명만 출력하면돼.  예시) 햇사과를 검색해줘. 예시 답안) 햇사과"

	result = assistant(role, task)
	
	return {"keyword" : result}


