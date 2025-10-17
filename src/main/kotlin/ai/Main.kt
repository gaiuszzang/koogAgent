package ai

import ai.agent.AgentProvider
import ai.agent.SimpleChatHistory
import ai.agent.ToolRegistryProvider
import ai.config.ConfigLoader
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

fun main() = runBlocking {
    println("Koog Agent Starting...")
    // Choose the service
    println("Choose the service")
    println("1. OpenAI\n2. Gemini\n3. Claude\n\n")
    print("You: ")
    val serviceIndex = withContext(Dispatchers.IO) { readlnOrNull() }?.trim()?.toIntOrNull() ?: return@runBlocking
    val apiKey = ConfigLoader.getApiKeyConfig()
    val executor = when (serviceIndex) {
        1 -> simpleOpenAIExecutor(apiKey.openai ?: error("OpenAI API key not found"))
        2 -> simpleGoogleAIExecutor(apiKey.gemini ?: error("Gemini API key not found"))
        3 -> simpleAnthropicExecutor(apiKey.claude ?: error("Claude API key not found"))
        else -> error("Invalid service index")
    }
    val model = when (serviceIndex) {
        1 -> OpenAIModels.Chat.GPT4o
        2 -> GoogleModels.Gemini2_5Flash
        3 -> AnthropicModels.Sonnet_4_5
        else -> error("Invalid service index")
    }
    val chatHistory = SimpleChatHistory()
    val toolRegistry = ToolRegistryProvider.provide(ConfigLoader.getMcpConfigList())

    // Interactive loop
    println("Agent is ready! Type your message (or '/exit' to quit)")
    println("-".repeat(50))

    while (true) {
        val agent = AgentProvider.provide(toolRegistry, executor, chatHistory, model)
        print("\nYou: ")
        System.out.flush() // Ensure a prompt is displayed
        val userInput = withContext(Dispatchers.IO) { readlnOrNull() }?.trim() ?: continue
        // Check for exit command
        if (userInput == "/exit") {
            println("Goodbye!")
            break
        }
        // Skip empty inputs
        if (userInput.isEmpty()) {
            continue
        }
        // Add user input to the chat history
        chatHistory.add(Message.User(content = userInput, metaInfo = RequestMetaInfo.create(Clock.System)))
        // Get agent response
        println("\nAgent: ")
        val response = agent.run(userInput)
        // Add agent response to the chat history
        chatHistory.add(Message.Assistant(content = response, metaInfo = ResponseMetaInfo.create(Clock.System)))
        println(response)
        println("-".repeat(50))
    }
}

