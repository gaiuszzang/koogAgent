package ai

import ai.cli.startCli
import ai.server.startServer

fun main(args: Array<String>) {
    val mode = args.find { it.startsWith("--mode=") }?.substringAfter("--mode=") ?: "cli"

    when (mode.lowercase()) {
        "server" -> {
            println("Starting Koog Agent in SERVER mode...")
            startServer(args)
        }
        "cli" -> {
            println("Starting Koog Agent in CLI mode...")
            startCli()
        }
        else -> {
            println("Invalid mode: $mode")
            println("Usage: --mode=cli (default) or --mode=server")
            System.exit(1)
        }
    }
}

