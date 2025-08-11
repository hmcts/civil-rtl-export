terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.39.0"
    }
  }
}
provider "azurerm" {
  features {}
  resource_provider_registrations = "none"
  alias                           = "private_endpoint"
  subscription_id                 = var.aks_subscription_id
}