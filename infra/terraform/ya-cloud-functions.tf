locals {
  oauth_token = data.sops_file.secrets.data["yandex_oauth_token"]
}

module "cloud_function" {
  source = "./modules/cloud_function"
  cron   = "*/15 8-23 ? * *"
  dir_with_function_code = "./control-spot-lifecycle-function"
  function_name = "start-spot-instance"
  oauth_token = local.oauth_token
  function_envs = {
    FOLDER_ID = data.yandex_resourcemanager_folder.this.folder_id
    INSTANCE_ID = yandex_compute_instance.this.id
    RUN_MODE = "starter"
  }
}

module "cloud_function" {
  source = "./modules/cloud_function"
  cron   = "59 23 ? * *"
  dir_with_function_code = "./control-spot-lifecycle-function"
  function_name = "stop-spot-instance"
  oauth_token = local.oauth_token
  function_envs = {
    FOLDER_ID = data.yandex_resourcemanager_folder.this.folder_id
    INSTANCE_ID = yandex_compute_instance.this.id
    RUN_MODE = "stopper"
  }
}
