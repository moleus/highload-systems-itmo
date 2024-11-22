@file:Suppress("UnstableApiUsage")


plugins {
    id("highload.application")
    id("highload.common")
    id("io.spring.dependency-management")
    id ("org.sonarqube") version "5.1.0.4882"
    id("highload.e2e-test")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")

    implementation("org.slf4j:slf4j-api")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito")
    }
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("io.projectreactor:reactor-test")

    testImplementation("org.springframework.kafka:spring-kafka-test")
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