variable "name" {
  description = "Name prefix for VPC resources."
  type        = string

  validation {
    condition     = length(trimspace(var.name)) > 0
    error_message = "VPC name must not be empty."
  }
}

variable "vpc_cidr" {
  description = "Primary IPv4 CIDR block for the VPC."
  type        = string
  default     = "10.20.0.0/16"
}

variable "availability_zones" {
  description = "Availability Zones used by the VPC."
  type        = list(string)

  validation {
    condition     = length(var.availability_zones) >= 3
    error_message = "At least three Availability Zones are required."
  }
}

variable "public_subnets" {
  description = "CIDRs for public subnets. One per AZ."
  type        = list(string)

  validation {
    condition     = length(var.public_subnets) == (length(var.availability_zones))
    error_message = "Number of public subnets must equal number of AZs."
  }
}

variable "private_app_subnets" {
  description = "CIDRs for private application subnets. One per AZ."
  type        = list(string)

  validation {
    condition = length(var.private_app_subnets) == length(var.availability_zones)

    error_message = "Number of private application subnets must equal number of AZs."
  }
}

variable "private_msk_subnets" {
  description = "CIDRs for private MSK subnets. One per AZ."
  type = list(string)

  validation {
    condition = length(var.private_msk_subnets) == length(var.availability_zones)
    error_message = "Number of MSK subnets must equal number of AZs."
  }
}

variable "enable_nat_gateway" {
  description = "Whether NAT Gateway infrastructure should be created."
  type = bool
  default = true
}

variable "single_nat_gateway" {
  description = "Use one NAT Gateway instead of one per AZ."
  type = bool
  default = false
}

variable "enable_dynamodb_endpoint" {
  description = "Create a DynamoDB Gateway VPC endpoint."
  type = bool
  default = true
}

variable "enable_flow_logs" {
  description = "Enable VPC Flow Logs."
  type = bool
  default = true
}

variable "flow_log_retention_days" {
  description = "CloudWatch retention period for VPC Flow Logs."
  type = number
  default = 30
}

variable "tags" {
  description = "Additional tags."
  type = map(string)
  default = {}
}