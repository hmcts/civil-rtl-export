variable "product" {}

variable "component" {}

variable "location" {
  default = "UK South"
}

variable "alert_location" {
  type = string
}

variable "env" {}

variable "subscription" {}

variable "common_tags" {
  type = map(string)
}

variable "team_contact" {
  type        = string
  description = "The name of your Slack channel people can use to contact your team about your infrastructure"
  default     = "#civil-sdt"
}

variable "destroy_me" {
  type        = string
  description = "Here be dragons! In the future if this is set to Yes then automation will delete this resource on a schedule. Please set to No unless you know what you are doing"
  default     = "No"
}

variable "aks_subscription_id" {
  type        = string
  description = "The AKS subscription id for the environment.  Set by pipeline."
}

variable "jenkins_AAD_objectId" {
  type        = string
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "database_backup_retention_days" {
  default     = 35
  description = "Backup retention period in days for the PGSql instance. Valid values are between 7 & 35 days"
}
