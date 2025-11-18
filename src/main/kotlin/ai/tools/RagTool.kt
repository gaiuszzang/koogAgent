package ai.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.rag.RagService
import kotlinx.coroutines.flow.toList

@Tool
@LLMDescription("Search for relevant documents about 'QuantumLeap AI'. Returns the content of the most relevant documents.")
suspend fun searchDocuments(
    @LLMDescription("Query to search relevant documents about")
    query: String,
): String {
    val ragService = RagService()
    val relevantDocuments = ragService.rankDocuments(query).toList()
    if (relevantDocuments.isEmpty()) {
        return "No relevant documents found for the query: $query"
    }

    val result = StringBuilder("Found ${relevantDocuments.size} relevant documents:\n\n")

    relevantDocuments.forEachIndexed { index, document ->
        result.append("Document ${index + 1}: ${document.document.docId()}\n")
        result.append("Content: ${document.document.text()}\n\n")
    }
    return result.toString()
}
