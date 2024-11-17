provider "kubernetes" {
  config_path    = "~/.kube/highload"
  config_context = "default"
}

locals {
  ssh_host = yandex_vpc_address.static_ip.external_ipv4_address[0].address
}

resource "null_resource" "copy_k3s_config" {
  triggers = {}
  provisioner "local-exec" {
    command = <<EOT
      ssh -i ${local.ssh_identity} ${local.ssh_user}@${local.ssh_host} "sudo cat /etc/rancher/k3s/k3s.yaml" > ~/.kube/highload
    EOT
  }
}

resource "kubernetes_namespace_v1" "dev" {
  metadata {
    name = "dev"
  }
}

resource "kubernetes_namespace_v1" "demo" {
  metadata {
    name = "demo"
  }
}
