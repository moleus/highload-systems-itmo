plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.jpa")
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
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("org.postgresql:postgresql")
//    implementation("org.liquibase:liquibase-core")
}

kotlin {
    compilerOptions {
        // infer Kotlin types from Spring API taking into account nullability
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
