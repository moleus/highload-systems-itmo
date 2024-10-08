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
                implementation(project(":services:animal:repositories"))
                implementation(project(":services:authentication:auth-repositories")) // for userRepository
                implementation("io.projectreactor:reactor-core")
            }
        }
    }
}

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:security"))
    implementation(project(":services:authentication:auth-repositories")) // for userRepository

    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("io.projectreactor:reactor-core")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

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
