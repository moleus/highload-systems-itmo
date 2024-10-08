import java.util.*

plugins {
    id("highload.web-conventions")
    id("highload.lint-conventions")
}

group = "ru.itmo"
version = "0.0.1-SNAPSHOT"

var jdkVersion = 21
var hostArchitecture = System.getProperty("os.arch").lowercase(Locale.getDefault())
if (hostArchitecture == "aarch64") {
    hostArchitecture = "arm64"
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server:4.1.3")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    implementation("com.google.code.gson:gson:2.10.1")
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
        image = "moleus/highload/eureka-server:dev"
    }
}
