plugins {
    id("highload.db")
    id("highload.web")

    id("highload.e2e-test")

    id("highload.application")
    id("highload.security")
}

dependencies {
    implementation(project(":shared:security"))
    implementation(project(":services:authentication:auth-repositories"))

    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
}
