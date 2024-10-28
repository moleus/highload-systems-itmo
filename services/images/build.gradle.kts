@file:Suppress("UnstableApiUsage")


plugins {
    id("highload.web")
    id("highload.application")
    id("highload.reactive-db")
    id("highload.security")
    id("highload.e2e-test")

    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:security"))
    implementation(project(":shared:db-migrations"))
    implementation(project(":shared:minio"))

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")
}

testing {
    suites {
        val integrationTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.minio:minio:8.5.13")
                implementation("org.testcontainers:minio:1.20.3")
                implementation("org.springframework.boot:spring-boot-starter-webflux")
            }
            sources {
                resources.srcDirs(
                    "src/integration-test/resources"
                )
            }
        }
    }
}

highloadApp {
    serviceName.set("transaction")
}