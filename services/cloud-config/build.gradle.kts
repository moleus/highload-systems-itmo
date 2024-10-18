plugins {
    id("highload.web")

    id("highload.application")
}

highloadApp {
    serviceName.set("cloud-config")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-config-server:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")
}

