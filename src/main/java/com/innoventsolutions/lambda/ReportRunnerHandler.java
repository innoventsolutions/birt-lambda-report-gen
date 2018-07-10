package com.innoventsolutions.lambda;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;

public class ReportRunnerHandler implements RequestHandler<ReportRunRequest, GatewayResponse> {
	public ReportRunnerHandler() {
		URL.setURLStreamHandlerFactory(new S3URLStreamHandlerFactory());
	}

	@Override
	public GatewayResponse handleRequest(final ReportRunRequest input, final Context context) {
		final String designUrl = input.getDesignUrl();
		final Map<String, String> parameters = input.getParameters();
		final String format = input.getFormat();
		final String outputBucket = input.getOutputBucket();
		final String outputKey = input.getOutputKey();
		final boolean runThenRender = input.isRunThenRender();
		final InputStream inputStream = this.getClass().getResourceAsStream(
			"birt-runner.properties");
		final Properties properties = new Properties();
		try {
			properties.load(inputStream);
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
		final Configuration.Editor editor = new Configuration.Editor();
		editor.loadProperties(properties);
		final Configuration configuration = new Configuration(editor);
		if (!configuration.doNotRun) {
			final BirtEnvironment env = new BirtEnvironment(configuration);
			final ReportRunner runner = new ReportRunner();
			try {
				final URLConnection connection = new URL(designUrl).openConnection();
				connection.setDoInput(true);
				final InputStream designInputStream = connection.getInputStream();
				try {
					final File tmpDir = new File("/tmp");
					final File tmpOutputFile = File.createTempFile("birt-report-output-", null,
						tmpDir);
					runner.engine = env.getReportEngine();
					runner.runReport(designInputStream, parameters, format,
						tmpOutputFile.getAbsolutePath(), runThenRender, env, configuration);
					final FileInputStream fis = new FileInputStream(tmpOutputFile);
					try {
						putS3Object(outputBucket, outputKey, fis, tmpOutputFile.length());
					}
					finally {
						fis.close();
					}
					tmpOutputFile.delete();
				}
				finally {
					designInputStream.close();
				}
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
		final Map<String, String> headers = new HashMap<>();
		final String body = "success";
		final GatewayResponse response = new GatewayResponse(false, 200, headers, body);
		return response;
	}

	protected PutObjectResult putS3Object(final String bucketName, final String key,
			final InputStream inputStream, final long contentLength) {
		final AmazonS3 s3Client = AmazonS3Client.builder().build();
		try {
			System.out.println("Uploading an object");
			final ObjectMetadata omd = new ObjectMetadata();
			return s3Client.putObject(bucketName, key, inputStream, omd);
		}
		catch (final AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which"
				+ " means your request made it "
				+ "to Amazon S3, but was rejected with an error response" + " for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		}
		catch (final AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means"
				+ " the client encountered " + "an internal error while trying to "
				+ "communicate with S3, " + "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
		return null;
	}
}
