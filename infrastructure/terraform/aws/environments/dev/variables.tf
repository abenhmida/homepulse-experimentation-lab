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