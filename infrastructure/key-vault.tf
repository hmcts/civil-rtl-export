data "azurerm_user_assigned_identity" "civil-mi" {
  name                = "${var.product}-${var.env}-mi"
  resource_group_name = "managed-identities-${var.env}-rg"
}

module "civil_rtl_export_key_vault" {
  source = "git@github.com:hmcts/cnp-module-key-vault?ref=master"

  name                        = "${var.product}-${var.component}-${var.env}"
  product                     = var.product
  env                         = var.env
  object_id                   = var.jenkins_AAD_objectId
  resource_group_name         = azurerm_resource_group.civil_rtl_export_rg.name
  product_group_name          = "DTS Civil"
  common_tags                 = local.tags
  managed_identity_object_ids = [data.azurerm_user_assigned_identity.civil-mi.principal_id]
}

data "azurerm_key_vault" "s2s_vault" {
  name                = "s2s-${var.env}"
  resource_group_name = "rpe-service-auth-provider-${var.env}"
}

data "azurerm_key_vault_secret" "key_from_vault" {
  name         = "microservicekey-civil-rtl-export"
  key_vault_id = data.azurerm_key_vault.s2s_vault.id
}

resource "azurerm_key_vault_secret" "s2s" {
  name         = "civil-rtl-export-service-s2s-secret"
  value        = data.azurerm_key_vault_secret.key_from_vault.value
  key_vault_id = module.civil_rtl_export_key_vault.key_vault_id
}
