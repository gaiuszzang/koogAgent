package ai.agent

import ai.config.ConfigLoader
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import ai.tools.InternalToolRegistryProvider
import ai.tools.McpToolRegistryProvider
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock

enum class AIService {
    OPENAI, GEMINI, CLAUDE
}

class AgentService(
    private val aiService: AIService
) {
    private var executor: PromptExecutor
    private var model: LLModel
    private var chatHistory: ChatHistory
    private var toolRegistry: ToolRegistry

    init {
        runBlocking {
            val apiKey = ConfigLoader.getApiKeyConfig()
            executor = when (aiService) {
                AIService.OPENAI -> simpleOpenAIExecutor(apiKey.openai ?: error("OpenAI API key not found"))
                AIService.GEMINI -> simpleGoogleAIExecutor(apiKey.gemini ?: error("Gemini API key not found"))
                AIService.CLAUDE -> simpleAnthropicExecutor(apiKey.claude ?: error("Claude API key not found"))
            }
            model = when (aiService) {
                AIService.OPENAI -> OpenAIModels.Chat.GPT4o
                AIService.GEMINI -> GoogleModels.Gemini2_5Flash
                AIService.CLAUDE -> AnthropicModels.Sonnet_4_5
            }
            chatHistory = SimpleChatHistory()
            toolRegistry = McpToolRegistryProvider.provide() + InternalToolRegistryProvider.provide()
        }
    }

    suspend fun chat(userInput: String): String {
        // Add user input to the chat history
        chatHistory.add(Message.User(content = userInput, metaInfo = RequestMetaInfo.create(Clock.System)))

        // Get agent response with a configured system prompt
        val systemPrompt = ConfigLoader.getSystemPrompt()
        val agent = AgentProvider.provide(toolRegistry, executor, chatHistory, model, systemPrompt)
        val response = agent.run(userInput)

        // Add agent response to the chat history
        chatHistory.add(Message.Assistant(content = response, metaInfo = ResponseMetaInfo.create(Clock.System)))
        return response
    }

    fun clearHistory() {
        chatHistory.clear()
    }

    fun getHistory(): List<Message> {
        return chatHistory.getChatHistory()
    }
}
