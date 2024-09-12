module "postgresql-v15" {
  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"

  providers = {
    azurerm.postgres_network = azurerm.private_endpoint
  }

  admin_user_object_id = var.jenkins_AAD_objectId
  business_area        = "cft"
  common_tags          = local.tags
  component            = var.component
  env                  = var.env
  pgsql_databases = [
    {
      name = "civil_rtl_export"
    }
  ]
  pgsql_version         = "15"
  product               = var.product
  name                  = join("-", [var.product, var.component, "v15"])
}

# Create secret for database user
resource "azurerm_key_vault_secret" "POSTGRES-USER-V15" {
  name         = "civil-rtl-export-POSTGRES-USER-V15"
  value        = module.postgresql-v15.username
  key_vault_id = module.civil_rtl_export_key_vault.key_vault_id
}

# Create secret for database password
resource "azurerm_key_vault_secret" "POSTGRES-PASS-V15" {
  name         = "civil-rtl-export-POSTGRES-PASS-V15"
  value        = module.postgresql-v15.password
  key_vault_id = module.civil_rtl_export_key_vault.key_vault_id
}

# Create secret for database host
resource "azurerm_key_vault_secret" "POSTGRES-HOST-V15" {
  name         = "civil-rtl-export-POSTGRES-HOST-V15"
  value        = module.postgresql-v15.fqdn
  key_vault_id = module.civil_rtl_export_key_vault.key_vault_id
}
