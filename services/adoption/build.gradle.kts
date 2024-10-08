@file:Suppress("UnstableApiUsage")

import java.util.*

plugins {
    id("highload.db-conventions")
    id("highload.web-conventions")
    id("highload.lint-conventions")
//    id("highload.e2e-test-conventions")
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
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            targets.all {
                testTask.configure {
                    setupEnvironment()
                }
            }
        }

        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter()
            testType.set(TestSuiteType.INTEGRATION_TEST)

            dependencies {
                implementation(project())
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-testcontainers")
                implementation("org.springframework.boot:spring-boot-starter-data-jpa")
                implementation("org.springframework.boot:spring-boot-starter-web")
                implementation("org.springframework.boot:spring-boot-starter-logging")
                implementation("org.testcontainers:testcontainers")
                implementation("org.testcontainers:postgresql")
                implementation("org.testcontainers:junit-jupiter")
                implementation("io.rest-assured:rest-assured")
                implementation(project(":shared:api"))
                implementation(project(":shared:db"))
                implementation(project(":shared:security"))
                implementation("org.liquibase:liquibase-core")
                runtimeOnly("org.postgresql:postgresql")

//                implementation(project(":services:animal:repositories"))
//                implementation(project(":services:authentication:auth-repositories")) // for userRepository
//                implementation("io.projectreactor:reactor-core")
            }

            sources {
                kotlin {
                    srcDir("src/integration-test/kotlin")
                }
                resources.srcDir("src/integration-test/resources")
            }

            // run integration tests after unit tests
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                        setupEnvironment()
                    }
                }
            }
        }
    }
}

fun Test.setupEnvironment() {
    environment("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", getDockerPathInsideContainer())
    environment("DOCKER_HOST", getDockerHostLocation())
}

fun getDockerPathInsideContainer() : String {
    if ("krot" == System.getenv("USER")) {
        return "/run/user/501/docker.sock"
    }
    return "/var/run/docker.sock"
}

fun getDockerHostLocation() : String {
    if ("krot" == System.getenv("USER")) {
        return "unix:///${System.getenv("HOME")}/.lima/docker/sock/docker.sock"
    }
    return "unix:///var/run/docker.sock"
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
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
