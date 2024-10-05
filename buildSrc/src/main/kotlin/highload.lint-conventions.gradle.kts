plugins {
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
}

group = "ru.itmo"
version = "0.0.1-SNAPSHOT"

detekt {
    config.setFrom("detekt.yml")
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
            useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
        }
    }
}
