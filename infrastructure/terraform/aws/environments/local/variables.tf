variable "environment" {
  description = "Deployment environment"
  type        = string

  validation {
    condition = contains(
      ["local", "dev", "staging", "prod"],
      var.environment
    )

    error_message = "Environment must be local, dev, staging, or prod."
  }
}

variable "aws_region" {
  description = "AWS region"
  type        = string
}

variable "enable_nat_gateway" {
  description = "Create NAT Gateway resources"
  type        = bool
  default     = true
}

variable "enable_dynamodb_endpoint" {
  description = "Create the DynamoDB VPC endpoint"
  type        = bool
  default     = true
}

variable "enable_flow_logs" {
  description = "Enable VPC Flow Logs"
  type        = bool
  default     = true
}

variable "project_name" {
  type        = string
  description = "Project name."
  default     = "homepulse"
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