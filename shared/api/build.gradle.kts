plugins {
    id("io.spring.dependency-management")
    id("highload.common")
    id ("java-library")

}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")
}

tasks.named("bootJar") {
    enabled = false
}