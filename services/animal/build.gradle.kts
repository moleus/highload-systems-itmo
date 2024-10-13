@file:Suppress("UnstableApiUsage")


plugins {
    id("highload.db")
    id("highload.web")
    id("highload.e2e-test")
    id("highload.application")
    id("highload.security")
}

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:security"))
    implementation(project(":services:authentication:auth-repositories")) // for userRepository

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")

    implementation("org.springframework.boot:spring-boot-starter-logging")
}

testing {
    suites {
        val integrationTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(project(":shared:db-migrations"))
                implementation(project(":shared:integration-tests"))
                implementation(testFixtures(project(":shared:integration-tests")))
                implementation("org.liquibase:liquibase-core")
                implementation("org.springframework.boot:spring-boot-starter-security:3.3.2")

                implementation("org.springframework.boot:spring-boot-starter-data-jpa")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
                implementation("org.springframework.boot:spring-boot-starter-validation")

                implementation("io.rest-assured:rest-assured")
                implementation("org.springframework.boot:spring-boot-starter-test")

                runtimeOnly("org.postgresql:postgresql")
            }
            sources {
                resources.srcDir(project(":shared:integration-tests").file("src/main/resources"))
            }
        }
    }
}
