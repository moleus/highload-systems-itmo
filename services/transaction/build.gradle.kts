@file:Suppress("UnstableApiUsage")


plugins {
    id("highload.web")

    id("highload.e2e-test")
    id("highload.application")
    id("io.spring.dependency-management")
    id("org.springframework.boot")

    id("highload.reactive-db")
    id("highload.security")
}

dependencies {
    implementation(project(":shared:api"))
    api(project(":shared:security"))
    implementation(project(":shared:db-migrations"))
    implementation("io.projectreactor:reactor-core")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")
}

testing {
    suites {
        val integrationTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
                implementation("org.springframework.data:spring-data-r2dbc")
                implementation(project(":shared:api"))
                implementation(project(":shared:security"))
                implementation(testFixtures(project(":shared:integration-tests")))
                implementation(project(":shared:integration-tests"))
                implementation("org.springframework.boot:spring-boot-starter-security:3.3.2")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
                implementation("org.testcontainers:testcontainers")
                implementation("org.testcontainers:r2dbc")
                runtimeOnly("org.postgresql:r2dbc-postgresql")
//                runtimeOnly("org.postgresql:postgresql")
            }
        }
    }
}

