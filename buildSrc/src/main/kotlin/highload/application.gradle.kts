package highload

import java.util.*

plugins {
    id("highload.common")
    id("com.google.cloud.tools.jib")
    id("org.springframework.boot")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("io.projectreactor:reactor-core")
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.3.2")
    implementation("org.springframework.kafka:spring-kafka")

    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
}

interface HighloadAppExtension {
    val serviceName: Property<String>
}

val applicationExtension = project.extensions.create<HighloadAppExtension>("highloadApp")

val hostArchitecture = System.getProperty("os.arch").lowercase(Locale.getDefault()).let {
    when {
        it.contains("aarch64") -> "arm64"
        else -> it
    }
}

val imageTag = System.getenv("IMAGE_TAG") ?: throw IllegalStateException("IMAGE_TAG is not set")

gradle.projectsEvaluated {
    jib {
        from {
            image = "ghcr.io/moleus/eclipse-temurin:21-jre"
            platforms {
                platform {
                    architecture = hostArchitecture
                    os = "linux"
                }
                platform {
                    architecture = "amd64"
                    os = "linux"
                }
            }
        }
        to {
            image = "ghcr.io/moleus/highload-systems-itmo/${applicationExtension.serviceName.get()}:${imageTag}"
        }
    }
}
