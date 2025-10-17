package ai.agent

import ai.config.McpConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.mcp.McpToolRegistryProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.header
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport
import io.modelcontextprotocol.kotlin.sdk.client.StreamableHttpClientTransport
import kotlin.collections.iterator

object ToolRegistryProvider {
    suspend fun provide(mcpConfigList: List<McpConfig>?): ToolRegistry {
        if (mcpConfigList.isNullOrEmpty()) return ToolRegistry.EMPTY
        val toolRegistries = mutableListOf<ToolRegistry>()
        mcpConfigList.forEach { mcpConfig ->
            toolRegistries.add(provideMcpToolRegistry(mcpConfig))
        }
        return toolRegistries.fold(toolRegistries.first()) { acc, registry -> acc + registry }
    }

    private suspend fun provideMcpToolRegistry(mcpConfig: McpConfig): ToolRegistry {
        if (mcpConfig.url == null) {
            throw IllegalArgumentException("MCP config url is null")
        }
        val transport = when (mcpConfig.type) {
            McpConfig.Type.sse, null -> SseClientTransport(createHttpClient(mcpConfig.headers), mcpConfig.url)
            McpConfig.Type.streamableHttp -> StreamableHttpClientTransport(createHttpClient(mcpConfig.headers), mcpConfig.url)
        }
        return McpToolRegistryProvider.fromTransport(transport)
    }

    private fun createHttpClient(headers: Map<String, String>?): HttpClient {
        return HttpClient {
            install(ContentNegotiation)
            install(SSE)
            defaultRequest {
                if (headers != null) {
                    for (headerItem in headers) {
                        header(headerItem.key, headerItem.value)
                    }
                }
            }
        }
    }
    /*
    private suspend fun provideReminderMcpSseToolRegistry(): ToolRegistry {
        val transport = McpToolRegistryProvider.defaultSseTransport(
            url = "https://reminder.mcp.groovin.io/sse",
            baseClient = HttpClient {
                install(ContentNegotiation)
                defaultRequest {
                    header("Authorization", "Bearer TWNwSnNTdG9ja1Byb2plY3QtU2hvd01lVGhlTW9uZXk=")
                }
            }
        )
        return McpToolRegistryProvider.fromTransport(transport)
    }*/
}
