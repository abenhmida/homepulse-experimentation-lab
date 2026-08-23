locals {
  common_tags = merge(
    {
      Name : var.name
      ManagedBy = "terraform"
      Component = "network"
    },
    var.tags
  )

  nat_gateway_count = (var.enable_nat_gateway ? (var.single_nat_gateway ? 1 : length(var.availability_zones)) : 0)
}
