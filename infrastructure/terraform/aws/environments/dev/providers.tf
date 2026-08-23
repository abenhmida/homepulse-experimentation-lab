
provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      project     = var.project_name
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}
