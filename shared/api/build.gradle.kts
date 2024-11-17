plugins {
    id("io.spring.dependency-management")
    id("highload.common")
    id ("org.sonarqube") version "5.1.0.4882"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")
}

sonarqube {
    properties {
        property("sonar.projectKey", "highload-systems-itmo-shared")
        property("sonar.projectName", "Highload Systems ITMO - shared")
        property("sonar.host.url", "http://89.169.129.100:9000")
        property("sonar.login", "3c90927b1bad2d789ea51a4909e613aca9c4253a")
        property("sonar.sourceEncoding", "UTF-8")
    }
}