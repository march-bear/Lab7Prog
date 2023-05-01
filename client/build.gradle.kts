plugins {
    application
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
    id("org.jetbrains.dokka") version "1.7.20"
}

group = "org.itmo.client"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    "implementation"(project(":tools"))
    "implementation"("org.valiktor:valiktor-core:0.12.0")
    "implementation"("io.insert-koin:koin-core:3.3.3")
    "implementation"("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    "implementation"("org.jetbrains.kotlin:kotlin-stdlib")
    "testImplementation"("org.jetbrains.kotlin:kotlin-test")
    "testImplementation"("io.mockk:mockk:1.13.4")
}

application {
    mainClass.set("MainKt")
}

tasks.compileKotlin{
    kotlinOptions.jvmTarget = "1.8"
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.jar {
    manifest.attributes["Main-Class"] = "MainKt"
    from(
        configurations
        .runtimeClasspath
        .get()
        .map { zipTree(it) }
    )
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}