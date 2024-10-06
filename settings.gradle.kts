rootProject.name = "highload"

include(
    "services:adoption",
    "services:animal",
    "services:api-gateway",
    "services:authentication",
    "services:cloud-config",
    "services:eureka-server",
    "services:notification",
    "services:transaction",
    "shared:security",
    "shared:api",
    "shared:db",
)
