aws_region   = "eu-west-3"
project_name = "homepulse"
environment  = "local"

# Disable features that don't work with Floci
enable_nat_gateway = false # Floci doesn't support NAT gateway well
# enable_vpn_gateway       = false # Not supported
enable_dynamodb_endpoint = false # VPC endpoints not supported
enable_flow_logs         = false # CloudWatch Logs may not work
#create_igw               = true  # Internet Gateway should work

availability_zones = [
  "eu-west-3a",
  "eu-west-3b",
  "eu-west-3c"
]
public_subnets = [
  "10.20.0.0/24",
  "10.20.1.0/24",
  "10.20.2.0/24"
]
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