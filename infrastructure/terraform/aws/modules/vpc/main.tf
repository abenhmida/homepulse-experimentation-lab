resource "aws_vpc" "this" {
  cidr_block = var.vpc_cidr

  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-vpc"
    }
  )
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-igw"
    }
  )
}

resource "aws_subnet" "public" {
  vpc_id = aws_vpc.this.id

  count = length(var.availability_zones)

  availability_zone = var.availability_zones[count.index]
  cidr_block        = var.public_subnets[count.index]

  map_public_ip_on_launch = true

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-public-${count.index + 1}"
      Tier = "public"
    }
  )
}

resource "aws_subnet" "private_app" {
  vpc_id = aws_vpc.this.id
  count  = length(var.availability_zones)

  availability_zone = var.availability_zones[count.index]
  cidr_block        = var.private_app_subnets[count.index]

  map_public_ip_on_launch = false

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-private-app-${count.index + 1}"
      Tier = "private-app"
    }
  )
}

resource "aws_subnet" "private_msk" {
  vpc_id = aws_vpc.this.id

  count = length(var.availability_zones)

  availability_zone = var.availability_zones[count.index]
  cidr_block        = var.private_msk_subnets[count.index]

  map_public_ip_on_launch = false

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-private-msk-${count.index + 1}"
      Tier = "private-msk"
    }
  )
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-public-rt"
    }
  )
}

resource "aws_route" "public_internet" {
  route_table_id = aws_route_table.public.id

  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.this.id
}

resource "aws_route_table_association" "public" {
  route_table_id = aws_route_table.public.id
  count          = length(aws_subnet.public)

  subnet_id = aws_subnet.public[count.index].id
}

resource "aws_eip" "nat" {
  count = local.nat_gateway_count

  domain = "vpc"

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-nat-eip-${count.index + 1}"
    }
  )
}

resource "aws_nat_gateway" "this" {
  count = local.nat_gateway_count

  allocation_id = aws_eip.nat[count.index].id

  subnet_id = aws_subnet.public[
    var.single_nat_gateway ? 0 : count.index
  ].id

  depends_on = [
    aws_internet_gateway.this
  ]

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-nat-${count.index + 1}"
    }
  )
}

resource "aws_route_table" "private_app" {
  vpc_id = aws_vpc.this.id
  count  = length(var.availability_zones)

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-private-app-${count.index + 1}-rt"
    }
  )
}

resource "aws_route" "private_app_internet" {
  route_table_id = aws_route_table.private_app[count.index].id

  count = var.enable_nat_gateway ? length(var.availability_zones) : 0

  destination_cidr_block = "0.0.0.0/0"

  nat_gateway_id = aws_nat_gateway.this[
    var.single_nat_gateway ? 0 : count.index
  ].id
}

resource "aws_route_table_association" "private_app" {
  count = length(aws_subnet.private_app)

  subnet_id = aws_subnet.private_app[count.index].id

  route_table_id = aws_route_table.private_app[count.index].id
}

resource "aws_route_table" "private_msk" {
  count = length(var.availability_zones)

  vpc_id = aws_vpc.this.id

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-private-msk-${count.index + 1}-rt"
    }
  )
}

resource "aws_route_table_association" "private_msk" {
  count = length(aws_subnet.private_msk)

  subnet_id = aws_subnet.private_msk[count.index].id

  route_table_id = aws_route_table.private_msk[count.index].id
}

resource "aws_vpc_endpoint" "dynamodb" {
  vpc_id = aws_vpc.this.id

  count        = var.enable_dynamodb_endpoint ? 1 : 0
  service_name = "com.amazonaws.${data.aws_region.current.region}.dynamodb"

  vpc_endpoint_type = "Gateway"

  route_table_ids = aws_route_table.private_app[*].id

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-dynamodb-endpoint"
    }
  )
}

data "aws_region" "current" {}

resource "aws_cloudwatch_log_group" "flow_logs" {
  count = var.enable_flow_logs ? 1 : 0

  name = "/aws/vpc/${var.name}"

  retention_in_days = var.flow_log_retention_days

  tags = local.common_tags
}

resource "aws_iam_role" "flow_logs" {
  count = var.enable_flow_logs ? 1 : 0

  name = "${var.name}-vpc-flow-logs"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"

    Statement = [
      {
        Effect = "Allow"

        Principal = {
          Service = "vpc-flow-logs.amazonaws.com"
        }

        Action = "sts:AssumeRole"
      }
    ]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy" "flow_logs" {
  count = var.enable_flow_logs ? 1 : 0

  name = "${var.name}-vpc-flow-logs"

  role = aws_iam_role.flow_logs[0].id

  policy = jsonencode({
    Version = "2012-10-17"

    Statement = [
      {
        Effect = "Allow"

        Action = [
          "logs:CreateLogStream",
          "logs:DescribeLogStreams",
          "logs:PutLogEvents"
        ]

        Resource = "${aws_cloudwatch_log_group.flow_logs[0].arn}:*"
      }
    ]
  })
}

resource "aws_flow_log" "this" {
  count = var.enable_flow_logs ? 1 : 0

  vpc_id = aws_vpc.this.id

  traffic_type = "ALL"

  iam_role_arn = aws_iam_role.flow_logs[0].arn

  log_destination = aws_cloudwatch_log_group.flow_logs[0].arn

  tags = merge(
    local.common_tags,
    {
      Name = "${var.name}-flow-logs"
    }
  )
}
