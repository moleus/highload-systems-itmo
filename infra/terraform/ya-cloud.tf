terraform {
  required_providers {
    yandex = {
      source = "yandex-cloud/yandex"
    }
    kubernetes = {
      source = "hashicorp/kubernetes"
    }
    sops = {
      source = "carlpett/sops"
      version = "~> 0.5"
    }
  }
  required_version = ">= 0.13"
}

locals {
  zone = "ru-central1-a"
  ssh_user = "yellow-duck-helping"
  ssh_identity = "~/.ssh/id_rsa"
}

provider "yandex" {
  zone = local.zone
}

data "yandex_resourcemanager_folder" "this" {
  name = "default"
}

# import {
#   id = "enps0o0esc3rvbe6n724"
#   to = yandex_vpc_network.this
# }
#
resource "yandex_vpc_network" "this" {
  description = "Auto-created network"
  folder_id = data.yandex_resourcemanager_folder.this.folder_id
}

resource "yandex_vpc_subnet" "this" {
  network_id = yandex_vpc_network.this.id
  name = "default-ru-central1-a"
  description = "Auto-created default subnet for zone ru-central1-a in default"
  folder_id = data.yandex_resourcemanager_folder.this.folder_id
  v4_cidr_blocks = ["10.128.0.0/24"]
}

resource "yandex_compute_instance" "this" {
  boot_disk {
    disk_id = ""
    initialize_params {
      image_id = "fd8hp9las7k42nhld0pe"
      size = 40
    }
  }
  name = "compute-vm-2-8-20-hdd-1731741773086"
  description = "highload k8s labs"
  metadata = {
    "enable-oslogin" = "true"
    "install-unified-agent" = "0"
    "user-data" = yamlencode({
      datasource = {
        Ec2 = {
          strict_id = false
        }
      }
      ssh_pwauth = "no"
    })
  }
  platform_id = "standard-v3"
  network_interface {
    subnet_id = yandex_vpc_subnet.this.id
    nat = true
    nat_ip_address = yandex_vpc_address.static_ip.external_ipv4_address[0].address
  }
  resources {
    core_fraction = 100
    cores = 4
    memory = 12
    gpus = 0
  }
}

resource "null_resource" "this" {
    triggers = {
        instance_id = yandex_compute_instance.this.id
    }
    provisioner "local-exec" {
        command = <<EOT
                k3sup install \
                --host ${yandex_compute_instance.this.network_interface[0].nat_ip_address} \
                --ip ${yandex_compute_instance.this.network_interface[0].nat_ip_address} \
                --ssh-key ${local.ssh_identity} \
                --user ${local.ssh_user} \
                --local-path $HOME/.kube/highload
            EOT
    }
}

resource "yandex_vpc_address" "static_ip" {
  name = "external-static-ip"
  external_ipv4_address {
    zone_id = local.zone
  }
}

output "external_ip" {
  value = yandex_vpc_address.static_ip.external_ipv4_address[0].address
}
