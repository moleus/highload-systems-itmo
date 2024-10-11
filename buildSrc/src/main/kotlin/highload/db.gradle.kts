package highload

import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("highload.common")
}

interface DatabaseExtension {
    val reactive: Property<Boolean>
}

val databaseExtension = project.extensions.create<DatabaseExtension>("highloadDatabase")
databaseExtension.reactive.convention(false)

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.liquibase:liquibase-core")

    if (databaseExtension.reactive.get()) {
        implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
//        implementation("io.r2dbc:r2dbc-postgresql")
        runtimeOnly("org.postgresql:r2dbc-postgresql")
    } else {
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        runtimeOnly("org.postgresql:postgresql")
    }
}
