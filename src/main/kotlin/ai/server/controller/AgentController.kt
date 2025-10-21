package ai.server.controller

import ai.server.dto.ChatRequest
import ai.server.dto.ChatResponse
import ai.agent.AIService
import ai.agent.AgentService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/agent")
class AgentController {

    @PostMapping("/chat")
    suspend fun chat(@RequestBody request: ChatRequest): ChatResponse {
        val aiService = when (request.service.lowercase()) {
            "openai" -> AIService.OPENAI
            "gemini" -> AIService.GEMINI
            "claude" -> AIService.CLAUDE
            else -> throw IllegalArgumentException("Invalid service: ${request.service}. Use 'openai', 'gemini', or 'claude'")
        }

        val agentService = AgentService(aiService)
        val response = agentService.chat(request.message)
        return ChatResponse(response = response)
    }

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "ok")
    }
}
