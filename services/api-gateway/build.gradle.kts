plugins {
    // don't import spring-web-conventions
    id("highload.application")
    id("io.spring.dependency-management")
}

highloadApp {
    serviceName.set("api-gateway")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway:4.1.5")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")

    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")
}
