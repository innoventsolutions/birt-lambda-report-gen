# Lambda-BIRT Integration
This project allows an AWS Lambda function to execute a BIRT report and deposit the report result to an S3 location.
### Notes
1. There has not been an official Maven dependency that works since version 4.4.  See https://www.eclipse.org/forums/index.php/t/1081625/.  But you can force Maven to use the BIRT runtime jars.
1. BIRT will not work in a "fat" jar because of OSGI, so Maven shade plugin cannot be used and a zip file must be constructed to be uploaded to Lambda.
1. Zip file is too big to be uploaded directly so must go to S3 first.

### Build Instructions
I was able to build with Maven by installing all the jar files from the BIRT runtime into my own Nexus server.  The included script file birt-maven-install does this.  However it isn't strictly necessary because we don't use the resulting jar file.  Instead we create a zip file consisting of the classes from this project and all the dependent jars.  The script file mkzip does this.  It gets the classes from target/classes, the BIRT jars from the BIRT runtime and other jars from the .m2 repository.  The fact that the eclipse project has the maven nature causes the classes to be deposited in target/classes and the .m2 repository to contain all the necessary jars just by opening the project in eclipse.  If you add all the BIRT jars to the build path then you don't need to run your own Nexus server and there will be no errors in eclipse, but you won't be able to build with maven.

To build without your own Nexus server:
1. Download BIRT runtime 4.8.0 from http://download.eclipse.org/birt/downloads/.
1. Clone the birt-lambda-report-gen project: git clone git@github.com:innoventsolutions/birt-lambda-report-gen.git
1. Open the project in eclipse and make sure the build path contains all the jar files from the BIRT runtime ReportEngine/lib folder.  You might need to remove those dependencies from pom.xml.
1. Modify mkzip if necessary to point to your .m2 repository and the BIRT runtime.
1. Execute mkzip.

To build with your own Nexus server:
1. Download BIRT runtime 4.8.0 from http://download.eclipse.org/birt/downloads/.
1. Clone the birt-lambda-report-gen project: git clone git@github.com:innoventsolutions/birt-lambda-report-gen.git
1. Modify birt-maven-install so it contains the correct path to your private Nexus repository and your BIRT runtime installation.
1. Execute birt-maven-install.
1. Build the project with maven:  mvn clean package
1. Modify mkzip if necessary to point to your .m2 repository and the BIRT runtime.
1. Execute mkzip.

### Installation Instructions
We will need to upload the zip file via S3 because it is too big for a direct upload.
We can create two different handlers that will be used in two different ways by the API Gateway (see Execution Instructions below).
1. Upload birt-lambda.zip from your computer to your S3 bucket.
1. Create a Lambda function by clicking Create Function on the Lambda console.
1. Set the runtime to Java 8.
1. In the Function Code section, choose Upload a file from Amazon S3.
1. Set the S3 Link URL to the URL shown in S3 when you click on the zip file.
1. Set the Handler to com.innoventsolutions.lambda.ProxyRequestHandler::handleRequest if you are going to execute with an API Gateway proxy request or com.innoventsolutions.lambda.JsonRequestHandler::handleRequest if you are going to execute with the Lambda function test button or an API Gateway non-proxy request.
1. If necessary, create a custom role using the IAM console: https://console.aws.amazon.com/iam/home#/roles
   1. Click the Create Role button
   1. Choose the Lambda service
   1. Permissions: AWSLambdaBasicExecutionRole, AmazonS3FullAccess
1. Set the timeout to a minute or more.

### Execution Instructions
There are three different ways to execute.
* Using the Test button on the Lambda function management page.
* Using API Gateway in non-proxy mode.
* Using API Gateway in proxy mode.

#### Example JSON code for requests
This is some example JSON code that can be used for the Test button and non-proxy API Gateway requests:
Note: sstest0323 is my S3 bucket name.  format can be any BIRT output format including pdf, xls, and html.

    {
      "designUrl": "s3:sstest0323/sqltest.rptdesign",
      "outputBucket": "sstest0323",
      "outputKey": "sql-test.html",
      "parameters": {
        "rowCount": 1
      },
      "format": "html",
      "runThenRender": false
    }

#### Example querystring parameters
This is an example of querystring parameters to be used with an API Gateway proxy request.  Note that all parameters other than the ones starting with double-underscores will be passed through to the report.

    __design_url=s3:sstest0323/test.rptdesign&__output_bucket=sstest0323&__output_key=test.pdf&rowCount=1&__format=pdf&__run_then_render=false

#### Using the Test button on the Lambda function management page
1. Upload a report design file to your S3 location.
1. Create a new test event containing JSON similar to the example JSON code above.
1. Click the run button.

#### Using the API Gateway in non-proxy mode
1. Upload a report design file to your S3 location.
1. In API Gateway, create a resource such as /report-runner and a POST method.
1. In Integration Request, specify the Lambda function pointing to JsonRequestHandler and make sure Use Lambda Proxy integration is unchecked.
1. Test it by inserting the JSON code (example above) into the body of the request.

#### Using the API Gateway in proxy mode
1. Upload a report design file to your S3 location.
1. In API Gateway, create a resource or reuse the same resource created above and create a GET method.
1. In Integration Request, specify the Lambda function pointing to ProxyRequestHandler and make sure Use Lambda Proxy integration is checked.
1. Test it by using the querystring parameters similar to the sample above.

#### Timing issues with API Gateway
Although you can set the timeout for a Lambda function to be as high as 5 minutes, the timeout for API Gateway is limited to 29 seconds.  When you run a report with API Gateway, and you exceed the timeout, the report can still finish but you will get an error from the API Gateway request.  To avoid this you can make the API Gateway request be asynchronous by setting the X-Amz-Invocation-Type HTTP header to "Event".  See https://docs.aws.amazon.com/lambda/latest/dg/API_Invoke.html.
