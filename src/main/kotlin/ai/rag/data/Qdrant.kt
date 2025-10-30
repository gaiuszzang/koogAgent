package ai.rag.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class QdrantPoint(
    val id: String,
    val vector: List<Float>,
    val payload: JsonObject
)

@Serializable data class QdrantUpsertReq(
    val points: List<QdrantPoint>
)


@Serializable data class QdrantSearchReq(
    // 단일 벡터 스키마 기준
    val vector: List<Float>,
    val limit: Int = 5,
    @SerialName("with_payload") val withPayload: Boolean = true,
    val filter: JsonObject? = null,
    val params: JsonObject? = null
)
@Serializable data class QdrantPointHit(
    val id: String? = null,
    val score: Double? = null,
    val payload: JsonObject? = null
) {
    fun docId(): String? = payload?.get("doc_id")?.jsonPrimitive?.contentOrNull
    fun section(): String? = payload?.get("section")?.jsonPrimitive?.contentOrNull
    fun extId(): String? = payload?.get("ext_id")?.jsonPrimitive?.contentOrNull
    fun text(): String? = payload?.get("text")?.jsonPrimitive?.contentOrNull
    override fun toString(): String {
        val score = score?.let { String.format("%.4f", it) } ?: "N/A"
        val text = text() ?: ""
        val preview = if (text.length > 200) text.take(200) + "..." else text
        return buildString {
            appendLine("id=${id ?: "N/A"}  score=$score  doc_id=${docId()}  section=${section()}  ext_id=${extId()}")
            appendLine(preview)
        }
    }

}


@Serializable data class QdrantSearchResp(
    val result: List<QdrantPointHit>? = null
)
