package ai.server.dto

data class ServiceRequest(
    val service: String // "openai", "gemini", or "claude"
)