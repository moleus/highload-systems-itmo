locals {
  deploy_key_path = "/Users/krot/.ssh/highload-cloud-config-ro"
}

 variable "github_ro_packages_token" {
  type = string
  sensitive = true
}

variable "github_email" {
  type = string
  sensitive = true
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
          password = var.github_ro_packages_token
          email    = var.github_email
        }
      }
    })
  }
}
