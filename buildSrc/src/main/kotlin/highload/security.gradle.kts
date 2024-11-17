package highload

plugins {
    id("io.spring.dependency-management")
    id("highload.common")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-security")
    api("io.jsonwebtoken:jjwt-api:0.12.6")

    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
}
