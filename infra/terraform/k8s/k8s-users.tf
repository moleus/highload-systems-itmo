provider "kubernetes" {
  config_path    = local.kubeconfig_path
  config_context = local.default_context
}

# module "yandex_base" {
#   source = "../"
# }
#
locals {
  default_context = "default"
  kubeconfig_path = "/Users/krot/.kube/highload"
  ssh_host = "51.250.0.147"
  developers = ["pavel", "kirill", "eva", "github-cd"]
  dev_namespaces = ["dev", "demo"]
  dev_ns_combinations = toset(flatten([for ns in local.dev_namespaces : [for dev in local.developers : { ns = ns, dev = dev }]]))
}

resource "kubernetes_namespace_v1" "this" {
  for_each = toset(local.dev_namespaces)
  metadata {
    name = each.value
  }
}

resource "kubernetes_role_v1" "dev_role" {
  for_each = toset(local.dev_namespaces)
  metadata {
    name = "${each.value}-developer-role"
    namespace = each.value
  }
  rule {
    api_groups = ["*", ""]
    resources = ["*"]
    verbs = ["*"]
  }
  depends_on = [kubernetes_namespace_v1.this]
}

resource "kubernetes_role_binding_v1" "dev_role_binding" {
  for_each = {
    for c in local.dev_ns_combinations : "${c.ns}-${c.dev}" => c
  }
  metadata {
    name = "${each.value.dev}-role-binding"
    namespace = each.value.ns
  }
  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind = "Role"
    name = "${each.value.ns}-developer-role"
  }
  subject {
    kind = "User"
    name = each.value.dev
  }
  depends_on = [kubernetes_namespace_v1.this]
}

module "k8s_user" {
  source = "../modules/k8s_user"
  for_each = toset(local.developers)

  user_name = each.value
  user_namespace = "dev"
  user_role = "${each.value}-developer-role"
  kubeconfig = local.kubeconfig_path
  depends_on = [kubernetes_namespace_v1.this]
}

// save kubeconfig to folder ~/.kube/{dev-name}. output is file content
resource "local_file" "kubeconfig" {
  for_each = toset(local.developers)
  content = module.k8s_user[each.key].kubeconfig
  filename = "/Users/krot/.kube/${each.key}"
  depends_on = [kubernetes_namespace_v1.this]
}
