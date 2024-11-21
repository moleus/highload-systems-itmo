locals {
  app_db_username = data.sops_file.secrets.data["app_db_username"]
  app_db_password = data.sops_file.secrets.data["app_db_password"]
}

resource "kubernetes_secret_v1" "db_secrets" {
  for_each = toset(local.dev_namespaces)
  metadata {
    name      = "db-secrets"
    namespace = each.value
  }
  data = {
    "username"     = sensitive(local.app_db_username)
    "password" = sensitive(local.app_db_password)
  }
}
