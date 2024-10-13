// this script builds plugins in buildSrc

plugins {
    id("org.springframework.boot")
    id("highload.common")
    id("highload.security")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web:3.3.2")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.3.2")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
}
