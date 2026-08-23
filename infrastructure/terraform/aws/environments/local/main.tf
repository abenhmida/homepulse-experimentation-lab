locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

module "vpc" {
  source = "../../modules/vpc"

  availability_zones = [
    "eu-west-3a",
    "eu-west-3b",
    "eu-west-3c"
  ]
  name = local.name_prefix
  private_app_subnets = [
    "10.20.10.0/24",
    "10.20.11.0/24",
    "10.20.12.0/24"
  ]
  private_msk_subnets = [
    "10.20.20.0/24",
    "10.20.21.0/24",
    "10.20.22.0/24"
  ]
  public_subnets = [
    "10.20.0.0/24",
    "10.20.1.0/24",
    "10.20.2.0/24"
  ]

  enable_nat_gateway = false
  # Cost-optimized dev topology.
  #single_nat_gateway = true

  enable_dynamodb_endpoint = false

  enable_flow_logs = false

  flow_log_retention_days = 7

  tags = {
    Owner = "homepulse"
  }
}