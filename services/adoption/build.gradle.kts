@file:Suppress("UnstableApiUsage")

import java.util.*

plugins {
    id("highload.db-conventions")
    id("highload.web-conventions")
    id("highload.lint-conventions")
    id("highload.e2e-test-conventions")
}

group = "ru.itmo"
version = "0.0.1-SNAPSHOT"


var jdkVersion = 21
var hostArchitecture = System.getProperty("os.arch").lowercase(Locale.getDefault())
if (hostArchitecture == "aarch64") {
    hostArchitecture = "arm64"
}

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:security"))
    implementation(project(":services:authentication:auth-repositories")) // for userRepository

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("io.projectreactor:reactor-core")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.12.0")
}

testing {
    suites {
        val integrationTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(project(":services:animal:repositories"))
                implementation(project(":services:authentication:auth-repositories")) // for userRepository
                implementation(project(":shared:api"))
                implementation(project(":shared:security"))
                implementation(project(":shared:db"))
                implementation(project(":services:animal:repositories"))
                implementation("org.springframework.boot:spring-boot-starter-security:3.3.2")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
                implementation("org.liquibase:liquibase-core")
                implementation("org.testcontainers:testcontainers")
                runtimeOnly("org.postgresql:postgresql")
            }
        }
    }
}

jib {
    from {
        image = "openjdk:$jdkVersion-jdk-slim"
        platforms {
            platform {
                architecture = hostArchitecture
                os = "linux"
            }
        }
    }
    to {
        image = "moleus/highload/adoption-service:dev"
    }
}
