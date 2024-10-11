// this script builds plugins in buildSrc

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("highload.common")
    id("highload.db")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation:3.3.2")
//    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
}
