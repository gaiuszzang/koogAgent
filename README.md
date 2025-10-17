# Koog Agent

A Kotlin-based AI agent project using the Koog framework.

## Overview

This project demonstrates how to build AI agents using the Koog framework, a Kotlin-based agent framework that provides a simple and powerful way to create intelligent agents.

**Features:**
- Multi-LLM support (OpenAI GPT-4o, Google Gemini 2.5 Flash, Claude Sonnet 4.5)
- MCP (Model Context Protocol) integration for external tool support
- Interactive chat interface with conversation history
- Coroutine-based async execution
- Structured logging with Logback

## Getting Started

### Prerequisites

- JDK 17 or higher
- Gradle 8.x (or use the Gradle wrapper)

### Configuration

**Important:** Before running the application, you must create a `config.json` file based on `config.json.example`:

1. Copy the example configuration file:
   ```bash
   cp config.json.example config.json
   ```

2. Edit `config.json` and add your API keys:
   ```json
   {
       "apiKey": {
           "openai": "your-openai-api-key-here",
           "gemini": "your-gemini-api-key-here",
           "claude": "your-claude-api-key-here"
       },
       "mcp": [
           {
               "url": "https://example.mcp.server/mcp",
               "type": "streamableHttp",
               "headers": {
                   "Authorization": "Bearer your-token-here"
               }
           }
       ]
   }
   ```

3. At least one API key is required depending on which service you want to use
4. MCP configuration is optional

### Build and Run

#### Option 1: Run with Gradle

1. Build the project:
   ```bash
   ./gradlew build
   ```

2. Run the application:
   ```bash
   ./gradlew run
   ```

#### Option 2: Install and Run Standalone

1. Build and install the distribution:
   ```bash
   ./gradlew installDist
   ```

2. Run the installed application:
   ```bash
   ./build/install/koogAgent/bin/koogAgent
   ```

3. Select your preferred AI service (OpenAI, Gemini, or Claude) and start chatting!

## License

MIT License
