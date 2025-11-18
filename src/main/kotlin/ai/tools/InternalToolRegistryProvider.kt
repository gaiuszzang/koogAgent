package ai.tools

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTool

object InternalToolRegistryProvider {
    fun provide(): ToolRegistry {
        return ToolRegistry {
            tool(::searchDocuments.asTool())
            tool(::getDateTime.asTool())
        }
    }
}
