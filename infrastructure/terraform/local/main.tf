terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "6.36.0"
    }
  }
}

provider "aws" {
  region                      = var.aws_region
  access_key                  = "local"
  secret_key                  = "local"
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    dynamodb = var.dynamodb_endpoint
  }
}

variable "aws_region" {
  type    = string
  default = "eu-west-3"
}

variable "dynamodb_endpoint" {
  type    = string
  default = "http://localhost:8000"
}

resource "aws_dynamodb_table" "device_state" {
  name         = "homepulse-device-state"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "pk"
  range_key    = "sk"

  attribute {
    name = "pk"
    type = "S"
  }

  attribute {
    name = "sk"
    type = "S"
  }

  tags = {
    project = "homepulse"
    env     = "local"
  }
}

resource "aws_dynamodb_table" "idempotency" {
  name         = "homepulse-idempotency"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "pk"

  attribute {
    name = "pk"
    type = "S"
  }

  tags = {
    project = "homepulse"
    env     = "local"
  }
}
