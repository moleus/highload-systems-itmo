@file:Suppress("UnstableApiUsage")

plugins {
    id("highload.common")
    id("highload.web")
//    id("highload.e2e-test")
    `java-test-fixtures`
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("io.rest-assured:rest-assured")
    implementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation(project(":shared:api"))
}
