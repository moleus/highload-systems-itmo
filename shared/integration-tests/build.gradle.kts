@file:Suppress("UnstableApiUsage")

plugins {
    id("highload.common")
    id("highload.web")
    `java-test-fixtures`
    java
}

dependencies {
    implementation("io.rest-assured:rest-assured")
    implementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.testcontainers:testcontainers")
    implementation("org.testcontainers:junit-jupiter")
    implementation("org.testcontainers:postgresql")
    implementation(project(":shared:db-migrations"))
    implementation("org.springframework.boot:spring-boot-testcontainers")
    testFixturesImplementation(project(":shared:api"))
}

sourceSets {
    main {
        resources {
            resources.setSrcDirs(files("src/main/non-existent"))
        }
    }
}
