@file:Suppress("UnstableApiUsage")


plugins {
    id("highload.application")
    id("highload.common")
    id("io.spring.dependency-management")
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
