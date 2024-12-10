locals {
  app_db_username = data.sops_file.secrets.data["app_db_username"]
  app_db_password = data.sops_file.secrets.data["app_db_password"]

  app_minio_username = data.sops_file.secrets.data["app_minio_username"]
  app_minio_password = data.sops_file.secrets.data["app_minio_password"]
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
  depends_on = [kubernetes_namespace_v1.this]
}

resource "kubernetes_secret_v1" "minio_secrets" {
  for_each = toset(local.dev_namespaces)
  metadata {
    name      = "minio-secrets"
    namespace = each.value
  }
  data = {
    "username"     = sensitive(local.app_minio_username)
    "password" = sensitive(local.app_minio_password)
  }
  depends_on = [kubernetes_namespace_v1.this]
}
