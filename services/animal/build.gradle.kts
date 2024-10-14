@file:Suppress("UnstableApiUsage")

plugins {
    id("highload.db")
    id("highload.web")
    id("highload.e2e-test")
    id("highload.application")
    id("highload.security")
}

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:security"))
    implementation(project(":services:animal:repositories")) // for userRepository
    implementation(project(":services:authentication:auth-repositories")) // for userRepository

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")

    implementation("org.springframework.boot:spring-boot-starter-logging")
}

testing {
    suites {
        val integrationTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(project(":services:animal:repositories"))
                implementation(project(":services:authentication:auth-repositories")) // for userRepository
                implementation(project(":services:animal:repositories"))
                implementation("org.springframework.boot:spring-boot-starter-security:3.3.2")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
            }
        }
    }
}
