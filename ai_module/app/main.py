from uuid import uuid4

from fastapi import FastAPI, HTTPException
from starlette.middleware.cors import CORSMiddleware

from app.schemas import ItineraryRequest
from app.backend_client import get_locations_for_city
from app.ollama_client import ask_ollama

app = FastAPI(title="AI Itinerary Planner")

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",
        "http://localhost:5173",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:5173",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

sessions: dict[str, dict] = {}


@app.post("/api/ai/itinerary")
async def itinerary_conversation(request: ItineraryRequest):
    # Continue existing conversation
    if request.sessionId:
        session = sessions.get(request.sessionId)

        if session is None:
            raise HTTPException(status_code=404, detail="Session not found")

        if not request.message:
            raise HTTPException(status_code=400, detail="Message is required")

        session["messages"].append({
            "role": "user",
            "content": request.message
        })

        answer = await ask_ollama(session["messages"])

        session["messages"].append({
            "role": "assistant",
            "content": answer
        })

        return {
            "sessionId": request.sessionId,
            "answer": answer
        }

    # Start new conversation
    if not request.days:
        raise HTTPException(status_code=400, detail="Days is required")

    selected_place = request.city or request.location

    if not selected_place:
        raise HTTPException(status_code=400, detail="City or location is required")

    locations = await get_locations_for_city(selected_place)

    prompt = f"""
You are an AI Itinerary Planner.

The user selected this city or location:
{selected_place}

Number of days:
{request.days}

User preferences:
{request.preferences}

Tourist objectives available from the application database:
{locations}

Task:
Generate an automatic travel itinerary in English.

Rules:
- Create a day-by-day itinerary.
- Include tourist objectives to visit.
- Include a clear schedule for each day.
- Use morning, afternoon and evening sections when useful.
- Use the user's preferences when choosing and presenting activities.
- Use the provided application objectives as the main recommendations.
- If the provided objectives are limited, you may suggest well-known tourist objectives for the selected city/location.
- Order the route from west to east when possible.
- Keep the answer practical, clear and friendly.
- After generating the itinerary, be ready to continue the conversation and adjust it based on the user's next messages.
"""

    messages = [
        {
            "role": "system",
            "content": "You are a helpful AI travel planner. Always answer in English."
        },
        {
            "role": "user",
            "content": prompt
        }
    ]

    answer = await ask_ollama(messages)

    session_id = str(uuid4())

    sessions[session_id] = {
        "selectedPlace": selected_place,
        "days": request.days,
        "preferences": request.preferences,
        "locations": locations,
        "messages": messages + [
            {
                "role": "assistant",
                "content": answer
            }
        ]
    }

    return {
        "sessionId": session_id,
        "answer": answer
    }