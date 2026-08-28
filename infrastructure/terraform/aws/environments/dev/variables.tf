variable "aws_region" {
  type        = string
  description = "AWS deployment region."
}

variable "project_name" {
  type        = string
  description = "Project name."
  default     = "homepulse"
}

variable "environment" {
  type        = string
  description = "Deployment environment."

  validation {
    condition = contains(
      ["dev", "staging", "prod"],
      var.environment
    )

    error_message = "Environment must be dev, staging, or prod."
  }
}

variable "enable_nat_gateway" {
  description = "Whether to create NAT gateways."
  type        = bool
  default     = true
}

variable "enable_dynamodb_endpoint" {
  description = "Whether to create the DynamoDB VPC endpoint."
  type        = bool
  default     = true
}

variable "enable_flow_logs" {
  description = "Whether to enable VPC Flow Logs."
  type        = bool
  default     = true
}

variable "availability_zones" {
  type = list(string)
}

variable "public_subnets" {
  type = list(string)
}

variable "private_app_subnets" {
  type = list(string)
}

variable "private_msk_subnets" {
  type = list(string)
}

variable "dynamodb_table_arns" {
  description = "DynamoDB tables the HomePulse application may access."
  type        = list(string)
  default     = []
}