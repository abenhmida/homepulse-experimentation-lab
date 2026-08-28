locals {
  common_tags = merge(
    var.tags, {
      Component   = "iam"
      Environment = var.environment
      ManagedBy   = "terraform"
      Owner       = "homepulse"
    }
  )
}

data "aws_iam_policy_document" "application_trust" {
  statement {
    sid     = "AllowTrustedRuntime"
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      identifiers = [var.application_trusted_service]
      type        = "Service"
    }
  }
}

resource "aws_iam_role" "application" {
  assume_role_policy = data.aws_iam_policy_document.application_trust.json

  name = "${var.name_prefix}-application"
  tags = local.common_tags
}

data "aws_iam_policy_document" "application_dynamodb" {
  statement {
    sid    = "DynamoDBStateAccess"
    effect = "Allow"
    actions = [
      "dynamodb:BatchGetItem",
      "dynamodb:BatchWriteItem",
      "dynamodb:ConditionCheckItem",
      "dynamodb:DeleteItem",
      "dynamodb:DescribeTable",
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:Query",
      "dynamodb:Scan",
      "dynamodb:UpdateItem"
    ]
    resources = var.dynamodb_table_arns
  }
}

resource "aws_iam_policy" "application_dynamodb" {
  count       = length(var.dynamodb_table_arns) > 0 ? 1 : 0
  name        = "${var.name_prefix}-application-dynamodb"
  description = "Least-privilege DynamoDB access for HomePulse."
  policy      = data.aws_iam_policy_document.application_dynamodb.json
  tags        = local.common_tags
}

resource "aws_iam_role_policy_attachment" "application_dynamodb" {
  count      = length(var.dynamodb_table_arns) > 0 ? 1 : 0
  role       = aws_iam_role.application.name
  policy_arn = aws_iam_policy.application_dynamodb[0].arn
}

data "aws_iam_policy_document" "application_msk" {
  dynamic "statement" {
    for_each = length(var.msk_cluster_arns) > 0 ? [1] : []
    content {
      sid       = "KafkaClusterAccess"
      effect    = "Allow"
      actions   = ["kafka-cluster:Connect", "kafka-cluster:DescribeCluster"]
      resources = var.msk_cluster_arns
    }
  }

  dynamic "statement" {
    for_each = length(var.msk_topic_arns) > 0 ? [1] : []
    content {
      sid       = "KafkaTopicAccess"
      effect    = "Allow"
      actions   = ["kafka-cluster:DescribeTopic", "kafka-cluster:ReadData", "kafka-cluster:WriteData"]
      resources = var.msk_topic_arns
    }
  }

  dynamic "statement" {
    for_each = length(var.msk_group_arns) > 0 ? [1] : []
    content {
      sid       = "KafkaConsumerGroupAccess"
      effect    = "Allow"
      actions   = ["kafka-cluster:DescribeGroup", "kafka-cluster:AlterGroup"]
      resources = var.msk_group_arns
    }
  }
}

resource "aws_iam_policy" "application_msk" {
  count = (
    length(var.msk_cluster_arns) > 0 ||
    length(var.msk_topic_arns) > 0 ||
    length(var.msk_group_arns) > 0
  ) ? 1 : 0

  name        = "${var.name_prefix}-application-msk"
  description = "Least-privilege MSK IAM authorization for HomePulse."
  policy      = data.aws_iam_policy_document.application_msk.json
  tags        = local.common_tags
}

resource "aws_iam_role_policy_attachment" "application_msk" {
  count = (
    length(var.msk_cluster_arns) > 0 ||
    length(var.msk_topic_arns) > 0 ||
    length(var.msk_group_arns) > 0
  ) ? 1 : 0

  role       = aws_iam_role.application.name
  policy_arn = aws_iam_policy.application_msk[0].arn
}

data "aws_iam_policy_document" "application_cloudwatch" {
  statement {
    sid       = "PublishHomePulseMetrics"
    effect    = "Allow"
    actions   = ["cloudwatch:PutMetricData"]
    resources = ["*"]

    condition {
      test     = "StringEquals"
      variable = "cloudwatch:namespace"
      values   = [var.cloudwatch_metric_namespace]
    }
  }
}

resource "aws_iam_policy" "application_cloudwatch" {
  name        = "${var.name_prefix}-application-cloudwatch"
  description = "Publish-only CloudWatch metrics for HomePulse."
  policy      = data.aws_iam_policy_document.application_cloudwatch.json
  tags        = local.common_tags
}

resource "aws_iam_role_policy_attachment" "application_cloudwatch" {
  role       = aws_iam_role.application.name
  policy_arn = aws_iam_policy.application_cloudwatch.arn
}

data "aws_iam_policy_document" "observability_trust" {
  statement {
    sid     = "AllowTrustedRuntime"
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = [var.observability_trusted_service]
    }
  }
}

resource "aws_iam_role" "observability" {
  name               = "${var.name_prefix}-observability"
  assume_role_policy = data.aws_iam_policy_document.observability_trust.json
  tags               = local.common_tags
}

data "aws_iam_policy_document" "observability_read" {
  statement {
    sid    = "ReadCloudWatch"
    effect = "Allow"
    actions = [
      "cloudwatch:DescribeAlarms",
      "cloudwatch:GetMetricData",
      "cloudwatch:GetMetricStatistics",
      "cloudwatch:ListMetrics"
    ]
    resources = ["*"]
  }

  statement {
    sid    = "ReadEC2Metadata"
    effect = "Allow"
    actions = [
      "ec2:DescribeAvailabilityZones",
      "ec2:DescribeInstances",
      "ec2:DescribeNetworkInterfaces",
      "ec2:DescribeSecurityGroups",
      "ec2:DescribeSubnets",
      "ec2:DescribeTags",
      "ec2:DescribeVpcs"
    ]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "observability_read" {
  name        = "${var.name_prefix}-observability-read"
  description = "Read-only AWS observability permissions for HomePulse."
  policy      = data.aws_iam_policy_document.observability_read.json
  tags        = local.common_tags
}

resource "aws_iam_role_policy_attachment" "observability_read" {
  role       = aws_iam_role.observability.name
  policy_arn = aws_iam_policy.observability_read.arn
}
