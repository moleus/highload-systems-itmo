@file:Suppress("UnstableApiUsage")


plugins {
    id("highload.application")
    id("highload.common")
    id("io.spring.dependency-management")
    id ("org.sonarqube") version "5.1.0.4882"
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")

    implementation("org.slf4j:slf4j-api")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
}

highloadApp {
    serviceName.set("notification")
}

sonarqube {
    properties {
        property("sonar.projectKey", "highload-systems-itmo-notification")
        property("sonar.projectName", "Highload Systems ITMO - notification")
        property("sonar.host.url", System.getenv("SONAR_HOST_URL") ?: "")
        property("sonar.login", System.getenv("SONAR_TOKEN") ?: "")
        property("sonar.sourceEncoding", "UTF-8")
    }
}