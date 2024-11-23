plugins {
    id("highload.reactive-db")
    id("highload.web")
    id("highload.e2e-test")
    id("highload.application")
    id("highload.security")
    id ("org.sonarqube") version "5.1.0.4882"
    id ("jacoco")
}

highloadApp {
    serviceName.set("authentication")
}

dependencies {
    implementation(project(":shared:security"))
    implementation(project(":shared:api"))
    implementation(project(":shared:db-migrations"))

    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")

}

sonar {
    properties {
        property("sonar.projectKey", "highload-systems-itmo-auth")
        property("sonar.projectName", "Highload Systems ITMO - auth")
        property("sonar.host.url", System.getenv("SONAR_HOST_URL") ?: "")
        property("sonar.login", System.getenv("SONAR_TOKEN") ?: "")
        property("sonar.sourceEncoding", "UTF-8")
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
    dependsOn(tasks.test)
}