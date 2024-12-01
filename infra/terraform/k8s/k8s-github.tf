locals {
  deploy_key_path = "/Users/krot/.ssh/highload-cloud-config-ro"
  github_ro_packages_token = data.sops_file.secrets.data["github_ro_packages_token"]
  github_email = data.sops_file.secrets.data["github_email"]
}

resource "kubernetes_secret_v1" "ro_deploy_key" {
  for_each = toset(local.dev_namespaces)
  metadata {
    name      = "github-deploy-key-ro"
    namespace = each.value
  }
  data = {
    "id_rsa" = file(local.deploy_key_path)
  }
  depends_on = [kubernetes_namespace_v1.this]
}

// image pull secrets for github container registry

resource "kubernetes_secret_v1" "ghcr_pull_secret" {
  for_each = toset(local.dev_namespaces)
  metadata {
    name      = "ghcr-registry"
    namespace = each.value
  }
  type = "kubernetes.io/dockerconfigjson"
  data = {
    ".dockerconfigjson" = jsonencode({
      auths = {
        "ghcr.io" = {
          username = "moleus"
          password = local.github_ro_packages_token
          email    = local.github_email
        }
      }
    })
  }
  depends_on = [kubernetes_namespace_v1.this]
}
