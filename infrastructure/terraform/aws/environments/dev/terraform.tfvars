aws_region   = "eu-west-3"
project_name = "homepulse"
environment  = "dev"

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
