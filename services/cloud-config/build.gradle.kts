plugins {
    id("highload.web")

    id("highload.application")
}

highloadApp {
    serviceName.set("cloud-config")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-config-server:4.1.3")
}

