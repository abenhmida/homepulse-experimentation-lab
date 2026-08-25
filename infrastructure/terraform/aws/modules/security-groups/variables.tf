variable "vpc_id" {
  description = "VPC where the security groups will be created"
  type = string
}

variable "name_prefix" {
  description = "Resource name prefix"
  type = string
}

variable "tags" {
  description = "Common resource tags"
  type = map(string)
  default = {}
}