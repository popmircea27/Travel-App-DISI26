# AI Itinerary Planner Module

AI-powered itinerary planner built with FastAPI and Ollama.

This module generates travel itineraries in English based on:
- selected city or location
- number of days
- user preferences

It also supports follow-up conversation based on the generated itinerary.

---

# Tech Stack

- FastAPI
- Ollama
- Docker
- Python 3.12

---

# Requirements

Before running this module:

- Docker Desktop must be installed
- The Spring Boot backend must already be running on:

```text
http://localhost:8080
```

Run the Module

Open a terminal inside the AI module folder:
```text
cd ai_module
```

run:
```text
docker compose up --build
```

# Generate New Itinerary

Do NOT send sessionId.

Example request:
```commandline
{
  "city": "Cluj-Napoca",
  "location": "Transilvania",
  "days": 7,
  "preferences": [
    "history",
    "food",
    "walking"
  ],
  "message": "Please generate an itinerary."
}
```

Example response:
```commandline
{
  "sessionId": "generated-session-id",
  "answer": "Generated itinerary..."
}
```
 # Continue Conversation
```text
{
  "sessionId": "generated-session-id",
  "message": "Can you make day 2 more relaxed?"
}
```