@file:Suppress("UnstableApiUsage")


plugins {
    id("highload.jdbc-e2e-test")
    id("highload.application")
    id("highload.db")
    id("highload.common")
}

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:security"))
    implementation(project(":shared:db-migrations"))
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.3.2")

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")

    implementation("org.slf4j:slf4j-api")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

highloadApp {
    serviceName.set("adoption")
}
