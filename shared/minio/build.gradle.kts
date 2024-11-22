plugins {
    id("io.spring.dependency-management")
    id("highload.common")
    id ("java-library")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("io.minio:minio:8.5.13")
}


tasks.named("bootJar") {
    enabled = false
}