package ai.server.dto

data class ChatRequest(
    val message: String,
    val service: String // "openai", "gemini", or "claude"
)