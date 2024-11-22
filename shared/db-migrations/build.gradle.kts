plugins {
    id("highload.common")
    id("highload.e2e-test")
    `java-test-fixtures`
    id ("java-library")
}

dependencies {
    testFixturesImplementation(project(":shared:api"))
}

tasks.named("bootJar") {
    enabled = false
}