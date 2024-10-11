plugins {
    // don't import spring-web-conventions
    id("highload.application")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway:4.1.5")
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.3.2")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")

    implementation("com.google.code.gson:gson:2.11.0")
}
