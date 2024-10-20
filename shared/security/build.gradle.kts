// this script builds plugins in buildSrc

plugins {
    id("io.spring.dependency-management")
    id("org.springframework.boot")
    id("highload.common")
    id("highload.security")
}

dependencies {
    implementation("io.projectreactor:reactor-core:3.6.10")
    implementation("org.springframework.boot:spring-boot-starter-security")
//    implementation("org.springframework.boot:spring-boot-starter-web:3.3.2")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.3.2")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")
}
