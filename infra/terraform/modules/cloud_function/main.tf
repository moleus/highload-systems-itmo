terraform {
  required_providers {
    yandex = {
      source = "yandex-cloud/yandex"
    }
  }
  required_version = ">= 0.13"
}


variable "function_name" {
  type = string
}

variable "function_envs" {
  type = map(string)
}

variable "dir_with_function_code" {
  type = string
}

variable "resource_manager_folder_name" {
  type = string
  default = "default"
}

variable "cron" {
  type = string
}

variable "oauth_token" {
  description = "OAuth token for Yandex.Cloud API"
  type = string
  sensitive = true
}

data "archive_file" "this" {
  type        = "zip"
  source_dir = var.dir_with_function_code
  output_path = "/tmp/terraform-${var.function_name}.zip"
}

resource "yandex_function" "this" {
  entrypoint = "index.handler"
  runtime    = "nodejs18"
  environment = var.function_envs
  memory = 128
  name = var.function_name
  service_account_id = yandex_iam_service_account.this.id
  user_hash = ""
  connectivity {
    network_id = var.vpc_network_id
  }
  secrets {
    environment_variable = "OAUTHTOKEN"
    id                   = yandex_lockbox_secret.oauth_token.id
    key                  = "key_token"
    version_id           = yandex_lockbox_secret_version.oauth_token_version.id
  }

  content {
    zip_filename = data.archive_file.this.output_path
  }

  log_options {
    disabled = true
  }
}

resource "yandex_function_trigger" "this" {
  name = "${var.function_name}-trigger"
  timer {
    cron_expression = var.cron
  }
  function {
    id = yandex_function.this.id
    service_account_id = yandex_iam_service_account.this.id
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
    text_value = var.oauth_token
  }
}

resource "yandex_iam_service_account" "this" {
  name = "cloud-function-restart-vms"
}

data "yandex_iam_policy" "admin" {
  binding {
    role = "functions.functionInvoker"

    members = [
      "serviceAccount:${yandex_iam_service_account.this.id}",
    ]
  }
  binding {
    role = "lockbox.payloadViewer"
    members = [
      "serviceAccount:${yandex_iam_service_account.this.id}",
    ]
  }
}

resource "yandex_iam_service_account_iam_policy" "function_invoke_and_see_secrets" {
  policy_data        = data.yandex_iam_policy.admin.policy_data
  service_account_id = yandex_iam_service_account.this.id
}
