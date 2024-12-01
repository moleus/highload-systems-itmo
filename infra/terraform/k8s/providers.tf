terraform {
  required_providers {
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

