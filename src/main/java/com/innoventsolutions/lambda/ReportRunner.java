package com.innoventsolutions.lambda;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLActionHandler;
import org.eclipse.birt.report.engine.api.HTMLCompleteImageHandler;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IEngineTask;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

public class ReportRunner {
	public static void main(final String[] args)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			IOException, SQLException, BirtException {
		final String designFile = args[0];
		final Map<String, String> parameters = new HashMap<>();
		final String[] paramStrings = args[1].split("&");
		for (final String paramString : paramStrings) {
			final int indexOfEq = paramString.indexOf("=");
			String key;
			String value;
			if (indexOfEq >= 0) {
				key = paramString.substring(0, indexOfEq);
				value = paramString.substring(indexOfEq + 1);
			}
			else {
				key = paramString;
				value = "";
			}
			parameters.put(key, value);
		}
		final String format = args[2];
		final String outputFilename = args[3];
		final boolean runThenRender = "true".equals(args[4]);
		final Configuration.Editor editor = new Configuration.Editor();
		editor.loadArgs(4, args);
		final Configuration configuration = new Configuration(editor);
		if (configuration.doNotRun) {
			return;
		}
		final FileInputStream fos = new FileInputStream(designFile);
		final BirtEnvironment env = new BirtEnvironment(configuration);
		final ReportRunner runner = new ReportRunner();
		runner.engine = env.getReportEngine();
		runner.runReport(fos, parameters, format, outputFilename, runThenRender, env,
			configuration);
	}

	private static final SimpleDateFormat PARAM_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	IReportEngine engine = null;

	@SuppressWarnings("unchecked")
	public int runReport(final InputStream inputStream, final Map<String, String> parameters,
			final String format, final String outputFilename, final boolean runThenRender,
			final BirtEnvironment env, final Configuration configuration)
			throws EngineException, IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, SQLException {
		final IReportRunnable design = engine.openReportDesign(inputStream);
		final long startTime = System.currentTimeMillis();
		System.out.println("Starting runReport at " + new Date());
		final IEngineTask task;
		if (runThenRender) {
			task = engine.createRunTask(design);
		}
		else {
			task = engine.createRunAndRenderTask(design);
		}
		final Map<String, Object> appContext = task.getAppContext();
		task.setAppContext(appContext);
		for (final String key : parameters.keySet()) {
			final String value = parameters.get(key);
			if (value.startsWith("{")) {
				// This will be a set of multi-selects
				final String stripBrack = value.substring(1, value.length() - 1);
				final String[] fieldStrings = stripBrack.split(", *");
				final Object[] fieldValues = new Object[fieldStrings.length];
				int i = 0;
				for (final String fieldString : fieldStrings) {
					fieldValues[i++] = getFieldObject(fieldString);
				}
				System.out.println("setting parameter " + key + " = " + fieldValues);
				task.setParameterValue(key, fieldValues);
			}
			else {
				final Object fieldValue = getFieldObject(value);
				System.out.println("setting parameter " + key + " = " + fieldValue);
				task.setParameterValue(key, fieldValue);
			}
		}
		task.validateParameters();
		if (task instanceof IRunAndRenderTask) {
			final IRunAndRenderTask rrTask = (IRunAndRenderTask) task;
			final RenderOption options = getRenderOptions(format, outputFilename, env);
			rrTask.setRenderOption(options);
			System.out.println("Executing runAndRender task");
			rrTask.run();
		}
		else if (task instanceof IRunTask) {
			final IRunTask runTask = (IRunTask) task;
			final int lastIndexOfDot = outputFilename.lastIndexOf(".");
			String docFilename;
			if (lastIndexOfDot >= 0) {
				docFilename = outputFilename.substring(0, lastIndexOfDot) + ".rptdocument";
			}
			else {
				docFilename = outputFilename + ".rptdocument";
			}
			final File docFile = new File(docFilename);
			System.out.println("Executing run task");
			runTask.run(docFile.getAbsolutePath());
			final IReportDocument rptdoc = engine.openReportDocument(docFile.getAbsolutePath());
			final IRenderTask renderTask = engine.createRenderTask(rptdoc);
			final RenderOption options = getRenderOptions(format, outputFilename, env);
			renderTask.setRenderOption(options);
			final long totalVisiblePageCount = renderTask.getTotalPage();
			renderTask.setPageRange("1-" + totalVisiblePageCount);
			System.out.println("Executing render task");
			renderTask.render();
			renderTask.close();
		}
		final List<EngineException> errors = task.getErrors();
		for (final EngineException engineException : errors) {
			System.out.println(engineException);
		}
		final long duration = System.currentTimeMillis() - startTime;
		System.out.println("duration = " + duration);
		return 1;
	}

	private RenderOption getRenderOptions(final String format, final String outputFilename,
			final BirtEnvironment env) {
		RenderOption options = null;
		if (format == null || format.equalsIgnoreCase(RenderOption.OUTPUT_FORMAT_HTML)) {
			final HTMLRenderOption htmlOption = new HTMLRenderOption();
			htmlOption.setOutputFormat(RenderOption.OUTPUT_FORMAT_HTML);
			htmlOption.setActionHandler(new HTMLActionHandler());
			htmlOption.setImageHandler(new HTMLCompleteImageHandler());
			htmlOption.setBaseImageURL(env.baseImageURL);
			htmlOption.setImageDirectory("images");
			options = htmlOption;
		}
		if (format.equalsIgnoreCase(RenderOption.OUTPUT_FORMAT_PDF)) {
			options = new PDFRenderOption();
			options.setOutputFormat(RenderOption.OUTPUT_FORMAT_PDF);
		}
		else if (format.equalsIgnoreCase("XLS")) {
			options = new RenderOption();
			options.setOutputFormat("XLS");
		}
		else if (format.equalsIgnoreCase("DOC")) {
			options = new RenderOption();
			options.setOutputFormat("DOC");
		}
		final File outputFile = new File(outputFilename);
		options.setOutputFileName(outputFile.getAbsolutePath());
		options.setOutputFormat(format);
		return options;
	}

	private Object getFieldObject(final String fieldString) {
		if ("true".equalsIgnoreCase(fieldString)) {
			return Boolean.TRUE;
		}
		if ("false".equalsIgnoreCase(fieldString)) {
			return Boolean.FALSE;
		}
		final String trimmedFieldString = fieldString.trim();
		if (trimmedFieldString.startsWith("\"") && trimmedFieldString.endsWith("\"")) {
			return trimmedFieldString.substring(1, trimmedFieldString.length() - 1);
		}
		try {
			final int intValue = Integer.parseInt(fieldString);
			return new Integer(intValue);
		}
		catch (final NumberFormatException e) {
		}
		try {
			final double dblValue = Double.parseDouble(fieldString);
			return new Double(dblValue);
		}
		catch (final NumberFormatException e) {
		}
		try {
			final Date valDate = PARAM_DATE_FORMAT.parse(fieldString);
			return new java.sql.Date(valDate.getTime());
		}
		catch (final ParseException e) {
		}
		return fieldString;
	}

	public static List<File> getPropFiles(final File baseDir) {
		final ArrayList<File> files = new ArrayList<File>();
		if (!baseDir.isDirectory()) {
			files.add(baseDir);
		}
		else {
			final File[] dirFile = baseDir.listFiles(new PropFilter());
			for (int i = 0; i < dirFile.length; i++) {
				files.add(dirFile[i]);
			}
		}
		return files;
	}

	private final static class PropFilter implements FilenameFilter {
		private final String extension = ".properties";

		@Override
		public boolean accept(final File dir, final String name) {
			return name.toLowerCase().endsWith(extension);
		}
	}
}
