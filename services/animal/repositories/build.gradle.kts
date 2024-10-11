@file:Suppress("UnstableApiUsage")

plugins {
    id("highload.db")
    id("highload.application")
}

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:security"))
    implementation(project(":shared:db"))
}
