rootProject.name = "highload"

include(
    "services:adoption",
    "services:animal",
    "services:animal:repositories",
    "services:api-gateway",
    "services:authentication",
    "services:authentication:auth-repositories",
    "services:cloud-config",
    "services:eureka-server",
    "services:notification",
    "services:transaction",
    "shared:security",
    "shared:api",
    "shared:db",
)
