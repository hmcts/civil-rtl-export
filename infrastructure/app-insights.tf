module "application_insights" {
  source = "git@github.com:hmcts/terraform-module-application-insights?ref=4.x"

  env                 = var.env
  product             = var.product
  name                = "${var.product}-${var.component}-appinsights"
  location            = var.location
  resource_group_name = azurerm_resource_group.civil_rtl_export_rg.name
  alert_limit_reached = true

  common_tags = var.common_tags
}

resource "azurerm_key_vault_secret" "appinsights-connection-string" {
  name         = "civil-rtl-export-appinsights-connection-string"
  value        = module.application_insights.connection_string
  key_vault_id = module.civil_rtl_export_key_vault.key_vault_id
}

