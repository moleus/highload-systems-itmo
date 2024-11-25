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
  oauth_token = data.sops_file.secrets.data["yandex_oauth_token"]
}

provider "yandex" {
  zone = local.zone
}

data "yandex_resourcemanager_folder" "this" {
  name = "default"
}

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
    core_fraction = 20
    cores = 2
    memory = 8
  }

  provisioner "local-exec" {
    command = <<EOT
            k3sup install \
            --ip ${self.network_interface[0].nat_ip_address} \
            --context k3s \
            --ssh-key ${local.ssh_identity}] \
            --user ${local.ssh_user}
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

data "archive_file" "restart_spot_instance" {
  type        = "zip"
  source_dir = "${path.module}/restart-instance-function"
  output_path = "/tmp/restart-instance-function.zip"
}

resource "yandex_function" "restart_spot_instance" {
  entrypoint = "index.handler"
  runtime    = "nodejs18"
  environment = {
    FOLDER_ID = data.yandex_resourcemanager_folder.this.folder_id
    INSTANCE_ID = yandex_compute_instance.this.id
  }
  memory = 128
  name = "restart-spot-instance"
  service_account_id = yandex_iam_service_account.function_restart_vms.id
  user_hash = ""
  connectivity {
    network_id = yandex_vpc_network.this.id
  }
  secrets {
    environment_variable = "OAUTHTOKEN"
    id                   = yandex_lockbox_secret.oauth_token.id
    key                  = "key_token"
    version_id           = yandex_lockbox_secret_version.oauth_token_version.id
  }

  content {
    zip_filename = data.archive_file.restart_spot_instance.output_path
  }

  log_options {
    disabled = true
  }
}

resource "yandex_function_trigger" "restart_spot_trigger" {
  name = "restart-spot-instance-trigger"
  timer {
    cron_expression = "*/15 8-23 ? * *"
  }
  function {
    id = yandex_function.restart_spot_instance.id
    service_account_id = yandex_iam_service_account.function_restart_vms.id
    tag = "$latest"
  }
}

resource "yandex_lockbox_secret" "oauth_token" {
  name = "oauth-token"
}

resource "yandex_lockbox_secret_version" "oauth_token_version" {
  secret_id = yandex_lockbox_secret.oauth_token.id
  entries {
    key = "key_token"
    text_value = local.oauth_token
  }
}

resource "yandex_iam_service_account" "function_restart_vms" {
  name = "cloud-function-restart-vms"
}

data "yandex_iam_policy" "admin" {
  binding {
    role = "functions.functionInvoker"

    members = [
      "serviceAccount:${yandex_iam_service_account.function_restart_vms.id}",
    ]
  }
  binding {
    role = "lockbox.payloadViewer"
    members = [
      "serviceAccount:${yandex_iam_service_account.function_restart_vms.id}",
    ]
  }
}

resource "yandex_iam_service_account_iam_policy" "function_invoke_and_see_secrets" {
  policy_data        = data.yandex_iam_policy.admin.policy_data
  service_account_id = yandex_iam_service_account.function_restart_vms.id
}

