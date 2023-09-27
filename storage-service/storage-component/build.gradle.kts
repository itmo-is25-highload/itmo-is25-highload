import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.spring.dependency-management") version "1.1.0"
    id("org.springframework.boot") version "3.0.6"
    id("java-library")
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.spring") version "1.8.10"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.10"
}

group = "ru.itmo.storage.storage"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Spring boot
    implementation("org.springframework.boot:spring-boot-starter")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Logger
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
    compileOnlyApi("io.github.oshai:kotlin-logging-jvm:5.1.0")
    runtimeOnly("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")

    // Kotlin libraries
    implementation(kotlin("stdlib-jdk8"))

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

allOpen {
    annotations(
        "org.springframework.stereotype.Component",
        "org.springframework.stereotype.Service",
        "org.springframework.stereotype.Repository",
        "org.springframework.stereotype.Configuration",
    )
}
