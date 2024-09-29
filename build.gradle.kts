@file:Suppress("UnstableApiUsage")

import org.springframework.boot.gradle.tasks.run.BootRun
import java.util.*

plugins {
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.ncorti.ktfmt.gradle") version "0.19.0"
    id("io.gitlab.arturbosch.detekt") version("1.23.6")
    id("com.google.cloud.tools.jib") version "3.4.2"
    kotlin("plugin.jpa") version "1.9.24"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    `jvm-test-suite`
}

group = "ru.itmo"
version = "0.0.1-SNAPSHOT"
var jdkVersion = 21

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    implementation("org.slf4j:slf4j-api")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.12.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
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
                implementation("org.springframework.boot:spring-boot-starter-security")
                implementation("org.springframework.security:spring-security-test")
                implementation("org.testcontainers:testcontainers")
                implementation("org.testcontainers:postgresql")
                implementation("org.testcontainers:junit-jupiter")
                implementation("io.rest-assured:rest-assured")
                // gson
                implementation("com.google.code.gson:gson:2.8.8")
            }

            sources {
                kotlin {
                    srcDir("src/integration-test/kotlin")
//                    resources.srcDir("src/main/resources")
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
    environment("SPRING_DATASOURCE_URL", System.getenv("SPRING_DATASOURCE_URL"))
    environment("SPRING_DATASOURCE_USERNAME", System.getenv("SPRING_DATASOURCE_USERNAME"))
    environment("SPRING_DATASOURCE_PASSWORD", System.getenv("SPRING_DATASOURCE_PASSWORD"))
    environment("JWT_SECRET_ACCESS", System.getenv("JWT_SECRET_ACCESS"))
    environment("JWT_SECRET_REFRESH", System.getenv("JWT_SECRET_REFRESH"))
    environment("JWT_EXPIRATION_ACCESS", 60)
    environment("JWT_EXPIRATION_REFRESH", 60)
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

tasks.getByName<BootRun>("bootRun") {
    systemProperty("jwt.secret.access", "0LHQvtC20LUg0L/QvtC80L7Qs9C4INC90LDQvCDQt9Cw0LrRgNGL0YLRjCDRjdGC0L7RgiDQv9GA0LXQtNC80LXRgg==" )
    systemProperty("jwt.secret.refresh", "0LfQsNGH0LXQvCDRgtGLINGN0YLQviDRh9C40YLQsNC10YjRjCDRjdGC0L4g0LLQvtC+0LHRidC1INGC0L4g0KHQldCa0KDQldCi")
    systemProperty("jwt.expiration.access", "60")
    systemProperty("jwt.expiration.refresh", "60")
    systemProperty("spring.datasource.url", "jdbc:postgresql://localhost:15432/postgres")
    systemProperty("spring.datasource.username", "postgres")
    systemProperty("spring.datasource.password", "postgres")
}

subprojects {
    apply(plugin = "com.ncorti.ktfmt.gradle")

    configure<com.ncorti.ktfmt.gradle.KtfmtExtension> {
        kotlinLangStyle()
    }
}

detekt {
    config.setFrom("detekt.yml")
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
        }
    }
}

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
        image = "moleus/highload-systems-itmo:dev"
    }
}