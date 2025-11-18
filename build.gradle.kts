plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.shadow)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    application
}

application.mainClass.set("ai.MainKt")


// Force all dependencies to use the same version of kotlinx-coroutines and kotlinx-serialization
configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlinx" && requested.name.startsWith("kotlinx-coroutines")) {
                useVersion("1.10.2")
            }//https://github.com/JetBrains/koog/issues/273
            if (requested.group == "org.jetbrains.kotlinx" && requested.name.startsWith("kotlinx-serialization")) {
                useVersion("1.8.1")
            }//because of two kotlinx-serialization versions in classpath (1.6.3 and 1.8.1)
        }
    }
}
dependencies {
    // Kotlinx dependencies
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.kotlinx.serialization.json)

    // Ktor dependencies
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)

    // Koog, mcp dependencies
    implementation(libs.koog.agents)
    implementation(libs.mcp.sdk)
    // Note : Yml base configuration, Executor injection support
    //implementation(libs.koog.spring.boot.starter) //https://github.com/JetBrains/koog/blob/develop/koog-spring-boot-starter/Module.md

    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // Note: logback-classic is already included via spring-boot-starter-logging
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.shadowJar {
    archiveBaseName.set("code-agent")
    mergeServiceFiles()
}
