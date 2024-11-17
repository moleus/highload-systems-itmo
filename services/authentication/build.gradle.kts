plugins {
    id("highload.reactive-db")
    id("highload.web")
    id("highload.e2e-test")
    id("highload.application")
    id("highload.security")
}

highloadApp {
    serviceName.set("authentication")
}

dependencies {
    implementation(project(":shared:security"))
    implementation(project(":shared:api"))
    implementation(project(":shared:db-migrations"))

    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")

}
