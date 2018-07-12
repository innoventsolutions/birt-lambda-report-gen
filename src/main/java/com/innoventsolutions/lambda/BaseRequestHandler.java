package com.innoventsolutions.lambda;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Properties;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;

public abstract class BaseRequestHandler {
	protected BaseRequestHandler() {
		URL.setURLStreamHandlerFactory(new S3URLStreamHandlerFactory());
	}

	protected ReportRunResponse handleRequest(final ReportRunRequest input) {
		final long startTime = System.currentTimeMillis();
		String designUrl = input.getDesignUrl();
		if (designUrl == null) {
			designUrl = System.getenv("designUrl");
		}
		String format = input.getFormat();
		if (format == null) {
			format = System.getenv("format");
		}
		String outputBucket = input.getOutputBucket();
		if (outputBucket == null) {
			outputBucket = System.getenv("outputBucket");
		}
		String outputKey = input.getOutputKey();
		if (outputKey == null) {
			outputKey = System.getenv("outputKey");
		}
		boolean runThenRender = input.isRunThenRender();
		if (!input.isRunThenRenderSet()) {
			runThenRender = "true".equals(System.getenv("runThenRender"));
		}
		final Map<String, String> parameters = input.getParameters();
		System.out.println("BaseRequestHandler.handleRequest");
		System.out.println("  designUrl = " + designUrl);
		System.out.println("  format = " + format);
		System.out.println("  outputBucket = " + outputBucket);
		System.out.println("  outputKey = " + outputKey);
		System.out.println("  runThenRender = " + runThenRender);
		System.out.println("  parameters = " + parameters);
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
					System.out.println("got report engine");
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
				final StringWriter stringWriter = new StringWriter();
				final PrintWriter printWriter = new PrintWriter(stringWriter);
				e.printStackTrace(printWriter);
				final ReportRunResponse response = new ReportRunResponse();
				response.setSuccess(false);
				response.setMessage(stringWriter.toString());
				response.setDuration(System.currentTimeMillis() - startTime);
				return response;
			}
		}
		final ReportRunResponse response = new ReportRunResponse();
		response.setSuccess(true);
		response.setDuration(System.currentTimeMillis() - startTime);
		return response;
	}

	protected static PutObjectResult putS3Object(final String bucketName, final String key,
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
