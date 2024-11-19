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

val labVersion = "lab3"

gradle.projectsEvaluated {
    jib {
        from {
            image = "public.ecr.aws/docker/library/eclipse-temurin:21-jre"
            platforms {
                platform {
                    architecture = hostArchitecture
                    os = "linux"
                }
            }
        }
        to {
            image = "moleus/highload/${applicationExtension.serviceName.get()}:${labVersion}"
        }
    }
}
