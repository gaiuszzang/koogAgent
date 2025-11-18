package ai.tools

import ai.config.ConfigLoader
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

object McpToolRegistryProvider {

    suspend fun provide(): ToolRegistry {
        val mcpConfigList = ConfigLoader.getMcpConfigList() ?: return ToolRegistry.EMPTY
        val toolRegistries = mutableListOf<ToolRegistry>()
        mcpConfigList.forEach { mcpConfig ->
            toolRegistries.add(mcpConfig.toToolRegistry())
        }
        return toolRegistries.fold(toolRegistries.first()) { acc, registry -> acc + registry }
    }

    private suspend fun McpConfig.toToolRegistry(): ToolRegistry {
        if (url == null) {
            throw IllegalArgumentException("MCP config url is null")
        }
        val httpClient = createHttpClient(headers)
        val transport = when (type) {
            McpConfig.Type.sse, null -> SseClientTransport(httpClient, url)
            McpConfig.Type.streamableHttp -> StreamableHttpClientTransport(httpClient, url)
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
}
