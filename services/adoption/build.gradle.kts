@file:Suppress("UnstableApiUsage")

plugins {
    id("highload.web")
    id("highload.e2e-test")
    id("highload.reactive-application")
    id("highload.db")
}

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:security"))
    implementation(project(":services:authentication:auth-repositories")) // for userRepository

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")
}

testing {
    suites {
        val integrationTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(project(":services:authentication:auth-repositories")) // for userRepository
                implementation(project(":shared:api"))
                implementation(project(":shared:security"))
                implementation(project(":shared:db-migrations"))
                implementation("org.springframework.boot:spring-boot-starter-security:3.3.2")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
                implementation("org.liquibase:liquibase-core")
                implementation("org.testcontainers:testcontainers")
                runtimeOnly("org.postgresql:postgresql")
            }
        }
    }
}

