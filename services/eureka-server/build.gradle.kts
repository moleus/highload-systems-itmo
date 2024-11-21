plugins {
    id("highload.web")
    id("highload.application")
}

highloadApp {
    serviceName.set("eureka-server")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
}
