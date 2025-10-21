package ai.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["ai.server"])
class ServerApplication

fun startServer(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}