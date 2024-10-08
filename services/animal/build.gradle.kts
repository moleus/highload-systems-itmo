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

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:security"))
    implementation(project(":shared:db"))
    implementation(project(":services:animal:repositories"))

    implementation("org.springframework.boot:spring-boot-starter-security:3.3.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
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
