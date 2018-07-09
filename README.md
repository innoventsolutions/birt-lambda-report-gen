# birt-lambda-report-gen

## Lambda-BIRT Integration
## Overview
This project allows an AWS Lambda function to execute a BIRT report and deposit the report result to an S3 location.
### Installation Instructions
Note - not finished, work in progress, do not use
1. Clone the birt-lambda-report-gen project: git clone git@github.com:innoventsolutions/birt-lambda-report-gen.git
1. Build the project: mvn clean package
1. Modify mkzip if necessary so it points to where you cloned the project by changing the PROJECT variable assignment on the second line.
1. Execute mkzip
1. Upload birt-lambda.zip to your S3 location
1. Create the Lambda function by clicking Create Function on the Lambda console.
1. Set the runtime to Java 8.
1. Upload birt-lambda.zip from your S3 location (it's too big to be uploaded from a file).
1. If necessary, create a custom role.
   1. Use the IAM console: https://console.aws.amazon.com/iam/home#/roles
   1. Click the Create Role button
   1. Choose the Lambda service
   1. Permissions: AWSLambdaBasicExecutionRole, AmazonS3FullAccess
