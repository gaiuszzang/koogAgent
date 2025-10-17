package ai.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.singleRunStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.RequestMetaInfo
import kotlinx.datetime.Clock

object AgentProvider {
    suspend fun provide(
        toolRegistry: ToolRegistry,
        promptExecutor: PromptExecutor,
        chatHistory: ChatHistory,
        model: LLModel
    ): AIAgent<String, String> {
        val prompt = Prompt(
            messages = chatHistory.getChatHistory() + Message.System(
                content = """You are a chatbot.""".trimIndent(),
                metaInfo = RequestMetaInfo.create(Clock.System)
            ),
            id = "chat"
        )

        val agentConfig = AIAgentConfig(
            prompt = prompt,
            model = model,
            maxAgentIterations = 50
        )

        val agent = AIAgent(
            promptExecutor = promptExecutor,
            agentConfig = agentConfig,
            strategy = singleRunStrategy(),
            toolRegistry = toolRegistry,
        ) {
            handleEvents {
                onToolCallStarting { ctx ->
                    println("Tool called: ${ctx.tool.name}")
                }
                onToolCallFailed { ctx ->
                    println("Tool call failed: ${ctx.tool.name}")
                    ctx.throwable.printStackTrace()
                }
                onToolCallCompleted { ctx ->
                    println("Tool call completed: ${ctx.tool.name}")
                    ctx.result?.let { println("Result: $it") }
                }
            }
        }
        return agent
    }

}
