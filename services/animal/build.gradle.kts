@file:Suppress("UnstableApiUsage")


plugins {
    id("highload.web")
    id("highload.application")
    id("highload.security")
    id("highload.reactive-db")
    id("highload.e2e-test")
    id("highload.common")
    id ("org.sonarqube") version "5.1.0.4882"
}

testing {
    suites {
        val integrationTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation("org.springframework.cloud:spring-cloud-contract-wiremock:4.1.4")
            }
        }
    }
}

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:security"))
    implementation(project(":shared:db-migrations"))

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")

    implementation("org.springframework.boot:spring-boot-starter-logging")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.10.RELEASE")
    implementation("com.playtika.reactivefeign:feign-reactor-spring-cloud-starter:4.2.1")

    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")
}

highloadApp {
    serviceName.set("animal")
}

sonarqube {
    properties {
        property("sonar.projectKey", "highload-systems-itmo-animal")
        property("sonar.projectName", "Highload Systems ITMO - animal")
        property("sonar.host.url", "http://89.169.129.100:9000")
        property("sonar.login", "3c90927b1bad2d789ea51a4909e613aca9c4253a")
        property("sonar.sourceEncoding", "UTF-8")
    }
}