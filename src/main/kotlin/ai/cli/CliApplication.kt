package ai.cli

import ai.agent.AIService
import ai.agent.AgentService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

class CliApplication {
    fun start() = runBlocking {
        println("Koog Agent Starting...")

        // Choose the service
        println("Choose the service")
        println("1. OpenAI\n2. Gemini\n3. Claude\n\n")
        print("You: ")
        val serviceIndex = withContext(Dispatchers.IO) { readlnOrNull() }?.trim()?.toIntOrNull() ?: return@runBlocking

        val aiService = when (serviceIndex) {
            1 -> AIService.OPENAI
            2 -> AIService.GEMINI
            3 -> AIService.CLAUDE
            else -> {
                println("Invalid service index")
                return@runBlocking
            }
        }

        val agentService = AgentService(aiService)

        // Interactive loop
        println("Agent is ready! Type your message (or '/exit' to quit)")
        println("-".repeat(50))

        while (true) {
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

            // Get agent response
            println("\nAgent: ")
            try {
                val response = agentService.chat(userInput)
                println(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            println("-".repeat(50))
        }
        exitProcess(0)
    }
}

fun startCli() {
    CliApplication().start()
}
