terraform {
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
    }
    tls = {
      source  = "hashicorp/tls"
    }
  }
}

variable "kubeconfig" {
  type = string
}

variable "user_name" {
  type = string
}

variable "user_namespace" {
  type = string
}

variable "user_role" {
  type = string
}

resource "tls_private_key" "user_privatekey" {
  algorithm = "RSA"
  rsa_bits  = 2048
}

resource "tls_cert_request" "user_csr" {
  private_key_pem = tls_private_key.user_privatekey.private_key_pem

  subject {
    common_name = var.user_name
  }
}

resource "kubernetes_certificate_signing_request_v1" "kubernetes_user_csr" {
  metadata {
    name = var.user_name
  }
  spec {
    usages      = ["client auth"]
    signer_name = "kubernetes.io/kube-apiserver-client"

    request = tls_cert_request.user_csr.cert_request_pem
  }

  auto_approve = true
}

resource "kubernetes_secret" "kubernetes_user_tls" {
  metadata {
    name = "${var.user_name}-tls"
    namespace = var.user_namespace
  }
  data = {
    "tls.crt" = kubernetes_certificate_signing_request_v1.kubernetes_user_csr.certificate
    "tls.key" = tls_private_key.user_privatekey.private_key_pem
  }
  type = "kubernetes.io/tls"
}

resource "kubernetes_role_binding_v1" "kubernetes_user_rolebinding" {
  metadata {
    name      = "${var.user_name}-${var.user_namespace}-${var.user_role}"
    namespace = var.user_namespace
  }
  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind      = "ClusterRole"
    name      = var.user_role
  }
  subject {
    kind      = "User"
    name      = var.user_name
    api_group = "rbac.authorization.k8s.io"
  }
}

output "client-certificate-data" {
  value = base64encode(kubernetes_certificate_signing_request_v1.kubernetes_user_csr.certificate)
  sensitive = true
}

output "client-key-data" {
  value = base64encode(tls_private_key.user_privatekey.private_key_pem)
  sensitive = true
}

data "local_file" "kubeconfig" {
  filename = var.kubeconfig
}

locals {
  kubeconfig_content = yamldecode(data.local_file.kubeconfig.content)
  new_context = {
    cluster = local.kubeconfig_content.contexts[0].context.cluster
    namespace = var.user_namespace
    user    = var.user_name
  }
  new_user = {
    name = var.user_name
    user = {
      "client-certificate-data" = base64encode(kubernetes_certificate_signing_request_v1.kubernetes_user_csr.certificate)
      "client-key-data"         = base64encode(tls_private_key.user_privatekey.private_key_pem)
    }
  }
  new_cluster = {
    cluster = {
      "certificate-authority-data" = local.kubeconfig_content.clusters[0].cluster.certificate-authority-data
      server                       = local.kubeconfig_content.clusters[0].cluster.server
    }
    name = local.kubeconfig_content.clusters[0].name
  }
}
// read kubeconfig file into a variable. replace context and user with the new user. Replace the certificate-authority-data with the new user's certificate

output "kubeconfig" {
  value = yamlencode({
    apiVersion     = "v1"
    clusters       = [local.new_cluster]
    contexts       = [{
      context = local.new_context
      name    = "${var.user_name}@kubernetes"
    }]
    current-context = "${var.user_name}@kubernetes"
    kind            = "Config"
    preferences     = {}
    users           = [local.new_user]
  })
}
