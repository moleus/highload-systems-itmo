plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")

    // lint-conventions
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.7")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.8.3")

    // spring-conventions
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.3.2")
    // jpa")
    implementation("org.jetbrains.kotlin:kotlin-noarg:1.9.24")
    // kotlin("spring")
    implementation("org.jetbrains.kotlin:kotlin-allopen:1.9.24")
}
