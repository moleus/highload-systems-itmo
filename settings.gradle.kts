rootProject.name = "highload"

include(
    "services:adoption",
    "services:animal",
    "services:animal:repositories",
    "services:api-gateway",
    "services:authentication",
    "services:cloud-config",
    "services:notification",
    "services:transaction",
    "services:balance",
    "services:images",
    "shared:security",
    "shared:web-security",
    "shared:webflux-security",
    "shared:api",
    "shared:db-migrations",
    "shared:integration-tests",
    "shared:minio",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
