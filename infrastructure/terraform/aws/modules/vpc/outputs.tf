output "vpc_id" {
  description = "VPC ID."
  value       = aws_vpc.this.id
}

output "vpc_cidr" {
  description = "VPC CIDR."
  value       = aws_vpc.this.cidr_block
}

output "availability_zones" {
  description = "Availability Zones."
  value       = var.availability_zones
}

output "public_subnet_ids" {
  description = "Public subnet IDs."
  value       = aws_subnet.public[*].id
}

output "private_app_subnet_ids" {
  description = "Private application subnet IDs."
  value       = aws_subnet.private_app[*].id
}

output "private_msk_subnet_ids" {
  description = "Private MSK subnet IDs."
  value       = aws_subnet.private_msk[*].id
}

output "public_route_table_id" {
  description = "Public route table ID."
  value       = aws_route_table.public.id
}

output "private_app_route_table_ids" {
  description = "Private application route table IDs."
  value       = aws_route_table.private_app[*].id
}

output "private_msk_route_table_ids" {
  description = "Private MSK route table IDs."
  value       = aws_route_table.private_msk[*].id
}

output "nat_gateway_ids" {
  description = "NAT Gateway IDs."
  value       = aws_nat_gateway.this[*].id
}

output "dynamodb_endpoint_id" {
  description = "DynamoDB Gateway Endpoint ID."
  value = try(
    aws_vpc_endpoint.dynamodb[0].id,
    null
  )
}