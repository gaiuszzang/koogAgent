package ai.server.controller

import ai.server.dto.ChatRequest
import ai.server.dto.ChatResponse
import ai.agent.AIService
import ai.agent.AgentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/agent")
class AgentController {

    @PostMapping("/chat")
    suspend fun chat(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        val aiService = when (request.service.lowercase()) {
            "openai" -> AIService.OPENAI
            "gemini" -> AIService.GEMINI
            "claude" -> AIService.CLAUDE
            else -> throw IllegalArgumentException("Invalid service: ${request.service}. Use 'openai', 'gemini', or 'claude'")
        }
        //TODO : Header 정보를 MCP Call 시 넘기고 싶다면 AgentService 생성 시점에 넘겨줘야 한다.
        val agentService = AgentService(aiService)
        return try {
            val response = agentService.chat(request.message)
            ResponseEntity.ok(ChatResponse(response = response))
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ChatResponse(response = e.message ?: "Unknown error"))
        }
    }

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "ok")
    }
}
