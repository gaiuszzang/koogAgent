package ai.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ApiKeyConfig(
    val openai: String? = null,
    val gemini: String? = null,
    val claude: String? = null,
)

@Serializable
data class McpConfig(
    val url: String? = null,
    val type: Type? = null,
    val headers: Map<String, String>? = null,
) {
    @Serializable
    enum class Type { sse, streamableHttp }
}

@Serializable
data class AgentConfig(
    val systemPrompt: String? = null
)

@Serializable
data class Config(
    val apiKey: ApiKeyConfig?,
    val mcp: List<McpConfig>?,
    val agent: AgentConfig? = null
)

object ConfigLoader {
    private val json = Json { ignoreUnknownKeys = true }
    private val config: Config by lazy {
        loadConfig()
    }

    private fun loadConfig(): Config {
        // Try to load config.json from the current working directory
        val configFile = File(System.getProperty("user.dir"), "config.json")

        if (!configFile.exists()) {
            throw IllegalStateException(
                "config.json not found in current directory: ${configFile.absolutePath}\n" +
                "Please create config.json based on config.json.example"
            )
        }

        return try {
            val configText = configFile.readText()
            json.decodeFromString<Config>(configText)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse config.json: ${e.message}", e)
        }
    }

    fun getApiKeyConfig(): ApiKeyConfig {
        return config.apiKey ?: ApiKeyConfig()
    }

    fun getMcpConfigList(): List<McpConfig>? {
        return config.mcp
    }

    fun getAgentConfig(): AgentConfig {
        return config.agent ?: AgentConfig()
    }

    fun getSystemPrompt(): String {
        return config.agent?.systemPrompt ?: "You are a chatbot."
    }
}
