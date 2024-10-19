plugins {
    id("highload.web")
    id("highload.application")
}

highloadApp {
    serviceName.set("eureka-server")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server:4.1.3")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
}
