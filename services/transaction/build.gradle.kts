plugins {
    id("highload.web")

    id("highload.e2e-test")
    id("highload.reactive-application")
    id("io.spring.dependency-management")
    id("org.springframework.boot")

    id("highload.reactive-db")
}

dependencies {
    implementation(project(":shared:api"))
    api(project(":shared:security"))
    implementation(project(":shared:db"))
    implementation("io.projectreactor:reactor-core")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.1.3")
    @Suppress("VulnerableDependency")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.1.3")
}
