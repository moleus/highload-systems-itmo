package highload

plugins {
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("highload.common")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
//        implementation("io.r2dbc:r2dbc-postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
}
