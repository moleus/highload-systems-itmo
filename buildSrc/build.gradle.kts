// this script builds plugins in buildSrc

plugins {
    `kotlin-dsl`
    id ("org.sonarqube") version "5.1.0.4882"
}

repositories {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
    implementation("com.google.cloud.tools:jib-gradle-plugin:3.4.4")

    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.7")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.8.3")

    // spring dependencies plugin
    implementation("io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:1.1.6")

    // spring-conventions
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.3.4")
    // kotlin("jpa")
    implementation("org.jetbrains.kotlin:kotlin-noarg:2.0.10")
    // kotlin("spring")
    implementation("org.jetbrains.kotlin:kotlin-allopen:2.0.10")
    implementation("com.google.cloud.tools:jib-gradle-plugin:3.4.2")
}


