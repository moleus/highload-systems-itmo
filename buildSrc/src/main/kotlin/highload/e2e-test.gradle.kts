@file:Suppress("UnstableApiUsage")

package highload

plugins {
    id("io.spring.dependency-management")
    id("highload.common")
}

dependencies {
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
                implementation(project(":shared:api"))
                implementation(project(":shared:db-migrations"))
                implementation(project(":shared:security"))
                implementation(project(":shared:integration-tests"))
                implementation(testFixtures(project(":shared:integration-tests")))
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-testcontainers")
                implementation("org.springframework.boot:spring-boot-starter-data-jpa")
                implementation("org.springframework.boot:spring-boot-starter-logging")
                implementation("org.testcontainers:testcontainers")
                implementation("org.testcontainers:postgresql")
                implementation("org.testcontainers:junit-jupiter")
                implementation("io.rest-assured:rest-assured")
                implementation("org.liquibase:liquibase-core")
                runtimeOnly("org.postgresql:postgresql")

                implementation("org.testcontainers:r2dbc")
                implementation("org.springframework.data:spring-data-r2dbc")
                runtimeOnly("org.postgresql:r2dbc-postgresql")
                implementation("io.projectreactor:reactor-core")
            }

            sources {
                kotlin {
                    srcDir("src/integration-test/kotlin")
                }
                resources.srcDir(project(":shared:integration-tests").file("src/main/resources"))
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
