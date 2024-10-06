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
