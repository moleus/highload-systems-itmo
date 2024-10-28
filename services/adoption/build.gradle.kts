@file:Suppress("UnstableApiUsage")


plugins {
    id("highload.jdbc-e2e-test")
    id("highload.application")
    id("highload.web")
    id("highload.db")
    id("highload.common")
}

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:security"))
    implementation(project(":shared:db-migrations"))

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")

    implementation("org.slf4j:slf4j-api")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")

}

highloadApp {
    serviceName.set("adoption")
}

testing {
    suites {
        val integrationTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation("org.springframework.cloud:spring-cloud-contract-wiremock:4.1.4")
                implementation("org.testcontainers:kafka:1.20.3")
            }
        }
    }
}
