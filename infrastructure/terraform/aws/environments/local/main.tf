locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

module "vpc" {
  source = "../../modules/vpc"

  availability_zones = var.availability_zones
  name = local.name_prefix
  private_app_subnets = var.private_app_subnets
  private_msk_subnets = var.private_msk_subnets
  public_subnets = var.public_subnets

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