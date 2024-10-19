package highload

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
}

group = "itmo.highload"
version = "0.2.0"

kotlin {
    compilerOptions {
        // infer Kotlin types from Spring API taking into account nullability
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

detekt {
    config.setFrom(rootDir.absolutePath + "/detekt.yml")
}

dependencies {
    api("io.github.oshai:kotlin-logging-jvm:7.0.0")
}

kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            @Suppress("UnstableApiUsage")
            useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
        }
    }
}
