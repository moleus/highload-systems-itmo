variable "apps_pg_user" {
  type = string
  sensitive = true
}

variable "apps_pg_password" {
  type = string
  sensitive = true
}

resource "kubernetes_secret_v1" "db_secrets" {
  for_each = toset(local.dev_namespaces)
  metadata {
    name      = "db-secrets"
    namespace = each.value
  }
  data = {
    "username"     = var.apps_pg_user
    "password" = var.apps_pg_password
  }
}
