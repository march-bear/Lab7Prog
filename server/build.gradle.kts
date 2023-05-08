plugins {
    application
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
    id("org.jetbrains.dokka") version "1.7.20"
}

group = "org.itmo.server"
version = "1.1"

repositories {
    mavenCentral()
}

application {
    mainClass.set("MainKt")
}

dependencies {
    implementation(project(":tools"))
    implementation("org.valiktor:valiktor-core:0.12.0")
    implementation("io.insert-koin:koin-core:3.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("ch.qos.logback:logback-core:1.2.9")
    implementation("org.postgresql:postgresql:42.2.27")
    implementation("com.google.guava:guava:31.1-jre")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.4")
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