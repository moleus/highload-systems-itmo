import java.util.*

plugins {
    // don't import spring-web-conventions
    id("org.springframework.boot")
    id("highload.lint-conventions")
    id("com.google.cloud.tools.jib")
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "ru.itmo"
version = "0.0.1-SNAPSHOT"

var jdkVersion = 21
var hostArchitecture = System.getProperty("os.arch").lowercase(Locale.getDefault())
if (hostArchitecture == "aarch64") {
    hostArchitecture = "arm64"
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway:4.1.5")
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.3.2")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")

    implementation("com.google.code.gson:gson:2.11.0")
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
        image = "moleus/highload/api-gateway:dev"
    }
}
