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

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE")
    implementation("com.playtika.reactivefeign:feign-reactor-spring-cloud-starter:4.2.1")
}

highloadApp {
    serviceName.set("transaction")
}

testing {
    suites {
        val integrationTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation("org.testcontainers:kafka:1.20.3")
            }
        }
    }
}
