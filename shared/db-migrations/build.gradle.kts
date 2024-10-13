plugins {
    id("highload.common")
    id("highload.e2e-test")
    `java-test-fixtures`
}

dependencies {
    testFixturesImplementation(project(":shared:api"))
}
