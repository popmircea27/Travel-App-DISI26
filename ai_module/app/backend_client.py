import os
import httpx

BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost:8080")


async def get_locations_for_city(city: str) -> list[dict]:
    async with httpx.AsyncClient(timeout=30) as client:
        response = await client.get(
            f"{BACKEND_URL}/api/locations",
            params={
                "locationName": city,
                "size": 100
            }
        )
        response.raise_for_status()
        return response.json()