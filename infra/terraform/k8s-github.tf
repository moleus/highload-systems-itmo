locals {
  deploy_key_path = "/Users/krot/.ssh/highload-cloud-config-ro"
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
