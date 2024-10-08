plugins {
    id("org.springframework.boot")
    id("com.google.cloud.tools.jib")
    kotlin("jvm")
    kotlin("plugin.spring")
}

repositories {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

group = "ru.itmo"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.slf4j:slf4j-api")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

kotlin {
    compilerOptions {
        // infer Kotlin types from Spring API taking into account nullability
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
