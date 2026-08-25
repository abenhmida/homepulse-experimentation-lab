terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "6.36.0"
    }
  }
}
resource "aws_security_group" "app" {
  name        = "${var.name_prefix}-app"
  description = "Security group for HomePulse application services"
  vpc_id      = var.vpc_id

  tags = merge(
    var.tags,
    {
      Name = "${var.name_prefix}-app"
    }
  )
}

resource "aws_vpc_security_group_egress_rule" "app_all" {
  cidr_ipv4 = "0.0.0.0/0"
  ip_protocol       = "-1"
  security_group_id = aws_security_group.app.id
}


resource "aws_security_group" "msk" {
  name = "${var.name_prefix}-msk"
  description = "Security group for Amazon MSK"
  vpc_id = var.vpc_id

  tags = merge(
    var.tags,
    {
      Name = "${var.name_prefix}-msk"
    }
  )
}

resource "aws_vpc_security_group_ingress_rule" "msk_from_app" {
  ip_protocol       = "tcp"
  security_group_id = aws_security_group.msk.id

  referenced_security_group_id = aws_security_group.app.id

  from_port = 9094
  to_port = 9094
}