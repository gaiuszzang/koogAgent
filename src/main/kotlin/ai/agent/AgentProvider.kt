package ai.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.AIAgentFunctionalStrategy
import ai.koog.agents.core.agent.FunctionalAIAgent
import ai.koog.agents.core.agent.GraphAIAgent.FeatureContext
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.context.AIAgentFunctionalContext
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.agent.singleRunStrategy
import ai.koog.agents.core.dsl.extension.asAssistantMessage
import ai.koog.agents.core.dsl.extension.containsToolCalls
import ai.koog.agents.core.dsl.extension.executeMultipleTools
import ai.koog.agents.core.dsl.extension.extractToolCalls
import ai.koog.agents.core.dsl.extension.requestLLM
import ai.koog.agents.core.dsl.extension.requestLLMMultiple
import ai.koog.agents.core.dsl.extension.sendMultipleToolResults
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.agent.chatAgentStrategy
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.agents.features.eventHandler.feature.EventHandlerConfig
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.agents.mcp.McpTool
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.params.LLMParams
import kotlinx.datetime.Clock

object AgentProvider {
    suspend fun provide(
        toolRegistry: ToolRegistry,
        promptExecutor: PromptExecutor,
        chatHistory: ChatHistory,
        model: LLModel,
        systemPrompt: String
    ): AIAgent<String, String> {
        val prompt = Prompt(
            messages = chatHistory.getChatHistory() + Message.System(
                content = systemPrompt,
                metaInfo = RequestMetaInfo.create(Clock.System)
            ),
            id = "chat",
            params = LLMParams(maxTokens = model.maxOutputTokens?.toInt())
        )

        val agentConfig = AIAgentConfig(
            prompt = prompt,
            model = model,
            maxAgentIterations = 50
        )

        val agent = AIAgent(
            promptExecutor = promptExecutor,
            agentConfig = agentConfig,
            strategy = chatAgentStrategy(), //singleRunStrategy(), //planningStrategy(),
            toolRegistry = toolRegistry,
        ) {
            handleEvents {
                onToolCallStarting { ctx ->
                    println("Tool called: ${ctx.tool.name}, ${ctx.toolArgs}")
                }
                onToolCallFailed { ctx ->
                    println("Tool call failed: ${ctx.tool.name}")
                    ctx.throwable.printStackTrace()
                }
                onToolCallCompleted { ctx ->
                    println("Tool call completed: ${ctx.tool.name}")
                    when (val result = ctx.result) {
                        is McpTool.Result -> println("Tool call response: ${result.textForLLM()}")
                        is String -> println("Tool call response: $result")
                        else -> println("Tool call response: $result")
                    }
                }
            }
        }
        return agent
    }

    private fun planningStrategy(): AIAgentFunctionalStrategy<String, String>  {
        return functionalStrategy { input ->
            // The first LLM call to produce an initial draft based on the user input
            println(" - functional : 플래닝하기")
            val planningPrompt = "사용자요청 : $input\n사용자 요청에 대해 너가 처리해야 하는 일들을 단계별로 계획해 봐. 계획 내용만 딱 정리해서 말하고 부차적인 말은 하지마."
            val planningResult = requestLLM(": $planningPrompt", allowToolCalls = false).asAssistantMessage().content
            println("  - input : $input")
            println("  - planning : $planningResult")
            val jobPrompt = "아래는 사용자 요청사항이야.\n - $input\n아래는 요청에 대한 너가 해야 하는 플랜이야.\n$planningResult\n작업을 순차적으로 하나하나 진행해 줘."

            var responses = requestLLMMultiple(jobPrompt)
            while (responses.containsToolCalls()) {
                val pendingCalls = extractToolCalls(responses)
                val results = executeMultipleTools(pendingCalls)
                responses = sendMultipleToolResults(results)
            }
            responses.single().asAssistantMessage().content
        }
    }
    /**
     * Note
     * GraphAIAgent.FeatureContext is implemented internally, but FunctionalAIAgent.FeatureContext is not.
     * So I added it here.
     */
    private fun FunctionalAIAgent.FeatureContext.handleEvents(configure: EventHandlerConfig.() -> Unit) {
        install(EventHandler) {
            configure()
        }
    }
}
