plugins {
    id("highload.common")
    id("highload.e2e-test")
    `java-test-fixtures`
    id ("org.sonarqube") version "5.1.0.4882"
}

dependencies {
    testFixturesImplementation(project(":shared:api"))
}

sonarqube {
    properties {
        property("sonar.projectKey", "highload-systems-itmo-db-migration")
        property("sonar.projectName", "Highload Systems ITMO - db-migration")
        property("sonar.host.url", "http://89.169.129.100:9000")
        property("sonar.login", "3c90927b1bad2d789ea51a4909e613aca9c4253a")
        property("sonar.sourceEncoding", "UTF-8")
    }
}