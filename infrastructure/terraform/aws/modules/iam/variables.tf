variable "name_prefix" {
  type = string
}

variable "environment" {
  type = string
  validation {
    condition     = contains(["dev", "staging", "prod", "local"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}

variable "application_trusted_service" {
  type    = string
  default = "ecs-tasks.amazonaws.com"
}

variable "observability_trusted_service" {
  type    = string
  default = "ecs-tasks.amazonaws.com"
}

variable "dynamodb_table_arns" {
  type    = list(string)
  default = []
}
variable "msk_cluster_arns" {
  type    = list(string)
  default = []
}
variable "msk_topic_arns" {
  type    = list(string)
  default = []
}
variable "msk_group_arns" {
  type    = list(string)
  default = []
}
variable "cloudwatch_metric_namespace" {
  type    = string
  default = "HomePulse"
}
variable "tags" {
  type    = map(string)
  default = {}
}
