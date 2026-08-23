locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

module "vpc" {
  source      = "../../modules/vpc"

  availability_zones = var.availability_zones
  name = local.name_prefix
  private_app_subnets = var.private_app_subnets
  private_msk_subnets = var.private_msk_subnets
  public_subnets = var.public_subnets

  enable_nat_gateway = var.enable_nat_gateway
  # Cost-optimized dev topology.
  single_nat_gateway = true

  enable_dynamodb_endpoint = var.enable_dynamodb_endpoint

  enable_flow_logs = var.enable_flow_logs

  flow_log_retention_days = 7

  tags = {
    Owner = "homepulse"
  }
}