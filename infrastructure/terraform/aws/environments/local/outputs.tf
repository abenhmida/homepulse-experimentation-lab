output "iam_application_role_name" {
  description = "IAM role used by HomePulse application workloads."
  value       = module.iam.application_role_name
}

output "iam_application_role_arn" {
  description = "IAM role ARN used by HomePulse application workloads."
  value       = module.iam.application_role_arn
}

output "iam_observability_role_name" {
  description = "IAM role used by HomePulse observability workloads."
  value       = module.iam.observability_role_name
}

output "iam_observability_role_arn" {
  description = "IAM role ARN used by HomePulse observability workloads."
  value       = module.iam.observability_role_arn
}

output "iam_application_cloudwatch_policy_arn" {
  description = "CloudWatch policy attached to the HomePulse application role."
  value       = module.iam.application_cloudwatch_policy_arn
}

output "iam_application_dynamodb_policy_arn" {
  description = "DynamoDB policy attached to the HomePulse application role."
  value       = module.iam.application_dynamodb_policy_arn
}

output "iam_application_msk_policy_arn" {
  description = "MSK policy attached to the HomePulse application role."
  value       = module.iam.application_msk_policy_arn
}