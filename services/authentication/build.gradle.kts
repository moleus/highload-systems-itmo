import java.util.*

plugins {
    id("highload.db-conventions")
    id("highload.web-conventions")
    id("highload.lint-conventions")
    id("highload.e2e-test-conventions")
}

group = "ru.itmo"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":shared:security"))
    implementation(project(":services:authentication:auth-repositories"))
    implementation("org.springframework.boot:spring-boot-starter-security:3.3.2")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
}

var jdkVersion = 21
var hostArchitecture = System.getProperty("os.arch").lowercase(Locale.getDefault())
if (hostArchitecture == "aarch64") {
    hostArchitecture = "arm64"
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
        image = "moleus/highload/authentication-service:dev"
    }
}
