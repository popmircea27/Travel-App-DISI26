import os
import httpx

OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "llama3.1")


async def ask_ollama(messages: list[dict]) -> str:
    async with httpx.AsyncClient(timeout=380) as client:
        response = await client.post(
            f"{OLLAMA_URL}/api/chat",
            json={
                "model": OLLAMA_MODEL,
                "messages": messages,
                "stream": False
            }
        )
        response.raise_for_status()

    data = response.json()
    return data["message"]["content"]