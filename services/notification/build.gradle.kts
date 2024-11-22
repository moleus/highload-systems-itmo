@file:Suppress("UnstableApiUsage")


plugins {
    id("highload.application")
    id("highload.common")
    id("io.spring.dependency-management")
    id("highload.e2e-test")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")

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
