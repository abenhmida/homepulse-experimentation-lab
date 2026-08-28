output "application_role_name" { value = aws_iam_role.application.name }
output "application_role_arn"  { value = aws_iam_role.application.arn }
output "observability_role_name" { value = aws_iam_role.observability.name }
output "observability_role_arn"  { value = aws_iam_role.observability.arn }
output "application_dynamodb_policy_arn" {
  value = try(aws_iam_policy.application_dynamodb[0].arn, null)
}
output "application_msk_policy_arn" {
  value = try(aws_iam_policy.application_msk[0].arn, null)
}
output "application_cloudwatch_policy_arn" {
  value = aws_iam_policy.application_cloudwatch.arn
}