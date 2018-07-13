package com.innoventsolutions.lambda;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.birt.core.exception.BirtException;
import org.junit.Test;

public class TestReport {
	@Test
	public void testRtrReport() throws IOException, BirtException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, SQLException {
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
		final BirtEnvironment env = new BirtEnvironment(configuration);
		final ReportRunner runner = new ReportRunner();
		runner.engine = env.getReportEngine();
		final InputStream designInputStream = this.getClass().getResourceAsStream("test.rptdesign");
		final Map<String, String> parameters = new HashMap<>();
		parameters.put("rowCount", "1");
		final String format = "pdf";
		final File outputFile = new File("test-rtr.pdf");
		runner.runReport(designInputStream, parameters, format, outputFile.getAbsolutePath(), true,
			env, configuration);
	}

	@Test
	public void testRarReport() throws IOException, BirtException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, SQLException {
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
		final BirtEnvironment env = new BirtEnvironment(configuration);
		final ReportRunner runner = new ReportRunner();
		runner.engine = env.getReportEngine();
		final InputStream designInputStream = this.getClass().getResourceAsStream("test.rptdesign");
		final Map<String, String> parameters = new HashMap<>();
		parameters.put("rowCount", "1");
		final String format = "pdf";
		final File outputFile = new File("test-rar.pdf");
		runner.runReport(designInputStream, parameters, format, outputFile.getAbsolutePath(), false,
			env, configuration);
	}
}
