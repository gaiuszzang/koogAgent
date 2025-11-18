package ai.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Tool
@LLMDescription("Get current date and time information")
suspend fun getDateTime(): String {
    val now = ZonedDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z (EEEE)")
    return "Current date and time: ${now.format(formatter)}"
}
