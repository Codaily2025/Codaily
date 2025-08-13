import time
import json
from pydantic import BaseModel
from fastapi import APIRouter, Request, HTTPException
from fastapi.responses import StreamingResponse
from langchain.chat_models import init_chat_model
from langchain_community.chat_message_histories import ChatMessageHistory
from src.specification.specification_service import *
from src.specification.specificastion_models import *
from src.specification.chat_service import *

router = APIRouter()

model = init_chat_model(model="gpt-4.1-nano", model_provider="openai", streaming=True)


@router.post("/intent")
async def determine_intent(req: MessageRequest):
    intent, feature_id, field = await classify_chat_intent(
        req.message, req.mainFeatures
    )

    return {"intent": intent, "featureId": feature_id, "field": field}


# 간단한 메모리
user_histories = {}


@router.get("/gpt/stream")
async def chat_stream(
    intent: ChatIntent,
    project_id: int,
    message: str,
    feature_id: int = None,
    title: str = None,
    field: str = None,
):
    print("project_id: ", project_id)
    history = user_histories.setdefault(project_id, ChatMessageHistory())
    history.add_user_message(message)

    params = {
        "feature_id": feature_id,
        "title": title,
        "field": field,
        "message": message,
    }

    def require(*names: str):
        missing = [n for n in names if params.get(n) in (None, "", [])]
        if missing:
            raise HTTPException(
                status_code=400, detail=f"Missing fields: {', '.join(missing)}"
            )

    match intent:
        case ChatIntent.CHAT:
            return StreamingResponse(
                gen_chat(history=history, intent=intent),
                media_type="text/event-stream",
            )

        case ChatIntent.SPEC | ChatIntent.SPEC_REGENERATE:
            formatted_text = format_history_to_text(history.messages)
            return StreamingResponse(
                gen_spec(formatted_text=formatted_text, wrapper_type=intent),
                media_type="text/event-stream",
            )

        case ChatIntent.SPEC_ADD_FIELD:
            require("message")
            return StreamingResponse(
                add_field(intent=intent, project_description=message, history=history),
                media_type="text/event-stream",
            )

        case ChatIntent.SPEC_ADD_FEATURE_MAIN:
            require("field")
            return StreamingResponse(
                add_main_feature(
                    intent=intent, message=message, field=field, history=history
                ),
                media_type="text/event-stream",
            )

        case ChatIntent.SPEC_ADD_FEATURE_SUB:
            require("feature_id", "title", "field")
            return StreamingResponse(
                add_sub_feature(
                    history=history,
                    intent=intent,
                    title=title,
                    field=field,
                    feature_id=feature_id,
                ),
                media_type="text/event-stream",
            )

        case _:
            raise HTTPException(
                status_code=400, detail=f"Unknown intent: {intent.value}"
            )
