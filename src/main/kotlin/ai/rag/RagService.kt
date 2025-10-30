package ai.rag

import ai.koog.embeddings.local.LLMEmbedder
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.rag.base.RankedDocument
import ai.koog.rag.base.RankedDocumentStorage
import ai.rag.data.QdrantPointHit
import ai.rag.data.QdrantSearchReq
import ai.rag.data.QdrantSearchResp
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlin.collections.orEmpty

//TODO : https://docs.koog.ai/ranked-document-storage/
class RagService: RankedDocumentStorage<QdrantPointHit> {
    companion object {
        const val DEFAULT_QDRANT_URL = "http://localhost:6333"
        const val DEFAULT_COLLECTION = "docs"
        const val DEFAULT_DISTANCE = "Cosine"
    }

    // Note : Embedding 관련 지원
    val embedder = LLMEmbedder(
        client = OllamaClient(baseUrl = "http://localhost:11434"),
        model = LLModel(
            provider = LLMProvider.Ollama,
            id = "bge-m3",
            capabilities = listOf(LLMCapability.Embed),
            contextLength = 1024,
        )
    )
    private val httpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }
    }

    override fun rankDocuments(query: String): Flow<RankedDocument<QdrantPointHit>> {
        return flow {
            val vector = embedder.embed(query)
            val qVec = vector.values.map { it.toFloat() }
            val list = search(
                queryVector = qVec,
                topK = 5,
                collection = DEFAULT_COLLECTION
            )
            list.forEach { item ->
                // Emit the document with its similarity score
                emit(RankedDocument(item, item.score ?: 0.0))
            }
        }
    }

    override suspend fun store(document: QdrantPointHit, data: Unit): String {
        TODO("Not yet implemented")
    }

    override suspend fun delete(documentId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun read(documentId: String): QdrantPointHit {
        TODO("Not yet implemented")
    }

    override fun allDocuments(): Flow<QdrantPointHit> {
        TODO("Not yet implemented")
    }

    /**
     * 벡터 검색
     *
     * @param queryVector 쿼리 벡터
     * @param topK 반환할 결과 개수
     * @param collection 컬렉션 이름
     * @param docId 필터링할 문서 ID (선택)
     * @param hnswEf HNSW 파라미터 (선택)
     * @return 검색 결과 리스트
     */
    suspend fun search(
        queryVector: List<Float>,
        topK: Int = 5,
        collection: String = DEFAULT_COLLECTION,
        docId: String? = null,
        hnswEf: Int? = null
    ): List<QdrantPointHit> {
        val filter = docId?.let {
            buildJsonObject {
                put("must", buildJsonArray {
                    add(buildJsonObject {
                        put("key", JsonPrimitive("doc_id"))
                        put("match", buildJsonObject { put("value", JsonPrimitive(it)) })
                    })
                })
            }
        }

        val params = hnswEf?.let {
            buildJsonObject { put("hnsw_ef", JsonPrimitive(it)) }
        }

        val req = QdrantSearchReq(
            vector = queryVector,
            limit = topK,
            withPayload = true,
            filter = filter,
            params = params
        )

        val resp = httpClient.post("$DEFAULT_QDRANT_URL/collections/$collection/points/search") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }

        if (resp.status.value !in 200..299) {
            error("Qdrant 검색 실패: ${resp.status} ${resp.bodyAsText()}")
        }

        val body = resp.bodyAsText()
        val json = Json { ignoreUnknownKeys = true }
        val parsed = json.decodeFromString<QdrantSearchResp>(body)
        return parsed.result.orEmpty()
    }
}
