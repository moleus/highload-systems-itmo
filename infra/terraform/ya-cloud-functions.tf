locals {
  oauth_token = data.sops_file.secrets.data["yandex_oauth_token"]
}

module "starter_function" {
  count = 0
  source = "./modules/cloud_function"
  cron   = "*/15 12-23 ? * *"  // UTC time
  dir_with_function_code = "./${path.module}/control-spot-lifecycle-function"
  function_name = "start-spot-instance"
  oauth_token = local.oauth_token
  function_envs = {
    FOLDER_ID = data.yandex_resourcemanager_folder.this.folder_id
    INSTANCE_ID = yandex_compute_instance.this.id
    RUN_MODE = "starter"
  }
  vpc_network_id = yandex_vpc_network.this.id
  function_sa_id = yandex_iam_service_account.this.id
}

module "stopper_function" {
  source = "./modules/cloud_function"
  cron   = "59 1 ? * *" // UTC time
  dir_with_function_code = "./${path.module}/control-spot-lifecycle-function"
  function_name = "stop-spot-instance"
  oauth_token = local.oauth_token
  function_envs = {
    FOLDER_ID = data.yandex_resourcemanager_folder.this.folder_id
    INSTANCE_ID = yandex_compute_instance.this.id
    RUN_MODE = "stopper"
  }
  vpc_network_id = yandex_vpc_network.this.id
  function_sa_id = yandex_iam_service_account.this.id
}

resource "yandex_iam_service_account" "this" {
  name = "cloud-function-restart-vms"
}

resource "yandex_resourcemanager_folder_iam_member" "function_invoker" {
  folder_id   = data.yandex_resourcemanager_folder.this.folder_id
  role        = "functions.functionInvoker"
  member      = "serviceAccount:${yandex_iam_service_account.this.id}"
}

resource "yandex_resourcemanager_folder_iam_member" "lockbox_access" {
  folder_id   = data.yandex_resourcemanager_folder.this.folder_id
  role        = "lockbox.payloadViewer"
  member      = "serviceAccount:${yandex_iam_service_account.this.id}"
}
