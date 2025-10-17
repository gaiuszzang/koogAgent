package ai.agent

import ai.koog.prompt.message.Message

interface ChatHistory {
    fun add(message: Message)
    fun clear()
    fun getChatHistory(): List<Message>
}


data class SimpleChatHistory(
    private val maxHistorySize: Int = 12
) : ChatHistory {
    private val chatHistoryList = mutableListOf<Message>()
    override fun add(message: Message) {
        chatHistoryList.addLast(message)
    }

    override fun clear() {
        chatHistoryList.clear()
    }

    override fun getChatHistory(): List<Message> {
        return chatHistoryList.takeLast(maxHistorySize)
    }
}
