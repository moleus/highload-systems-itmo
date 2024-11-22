@file:Suppress("UnstableApiUsage")


plugins {
    id("highload.web")
    id("highload.application")
    id("highload.reactive-db")
    id("highload.security")
    id("highload.e2e-test")

    id("io.spring.dependency-management")
    id("org.springframework.boot")
    id ("org.sonarqube") version "5.1.0.4882"
}

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:security"))
    implementation(project(":shared:db-migrations"))

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")
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

sonarqube {
    properties {
        property("sonar.projectKey", "highload-systems-itmo-transaction")
        property("sonar.projectName", "Highload Systems ITMO - transaction")
        property("sonar.host.url", System.getenv("SONAR_HOST_URL") ?: "")
        property("sonar.login", System.getenv("SONAR_TOKEN") ?: "")
        property("sonar.sourceEncoding", "UTF-8")
    }
}