@file:Suppress("UnstableApiUsage")


plugins {
//    id("highload.web")
    id("highload.e2e-test")
    id("highload.application")
    id("highload.db")
}

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:security"))
    implementation(project(":shared:db-migrations"))
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.3.2")

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")

    implementation("org.slf4j:slf4j-api")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

testing {
    suites {
        val integrationTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(project(":shared:api"))
                implementation(project(":shared:security"))
                implementation(project(":shared:db-migrations"))
                implementation(project(":shared:integration-tests"))
                implementation(testFixtures(project(":shared:integration-tests")))
                implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")

                implementation("org.springframework.boot:spring-boot-starter-security:3.3.2")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
                implementation("org.testcontainers:testcontainers")
                runtimeOnly("org.postgresql:postgresql")
            }
            sources {
                resources.srcDir(project(":shared:integration-tests").file("src/main/resources"))
            }
        }
    }
}

