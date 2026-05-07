from pydantic import BaseModel
from typing import Optional


class ItineraryRequest(BaseModel):
    sessionId: Optional[str] = None

    city: Optional[str] = None
    location: Optional[str] = None
    days: Optional[int] = None
    preferences: list[str] = []

    message: Optional[str] = None