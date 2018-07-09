package com.innoventsolutions.lambda;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.IDesignEngineFactory;

public class BirtEnvironment {
	// private static final Logger logger = Logger.getLogger( BirtEnvironment.class.getName() );
	final File workspace;
	final File birtRuntimeHome;
	final File resourcePath;
	final File scriptLib;
	final String reportFormat;
	final String baseImageURL;
	final String loggingDir;
	final String loggingPropertiesFile;

	public BirtEnvironment(final Configuration configuration) {
		// default directory for report design files
		workspace = configuration.workspace == null ? null : new File(configuration.workspace);
		// Eclipse installation directory, parent of plugins
		birtRuntimeHome = configuration.birtRuntimeHome == null ? null
			: new File(configuration.birtRuntimeHome);
		// Where report resources live
		if (configuration.resourcePath == null) {
			throw new NullPointerException("Resource path is missing");
		}
		resourcePath = new File(configuration.resourcePath);
		// Where event handler jar files live
		scriptLib = configuration.scriptLib == null ? null : new File(configuration.scriptLib);
		// Default report format
		reportFormat = configuration.reportFormat;
		// Location for images
		baseImageURL = configuration.baseImageURL;
		loggingDir = configuration.loggingDir;
		loggingPropertiesFile = configuration.loggingPropertiesFile;
	}

	public String logValues() {
		final StringBuilder sb = new StringBuilder();
		sb.append("\n\tWORKSPACE: ");
		sb.append(workspace == null ? "null" : workspace.getAbsolutePath());
		sb.append("\n\tBIRT_RUNTIME_HOME: ");
		sb.append(birtRuntimeHome == null ? "null" : birtRuntimeHome.getAbsolutePath());
		sb.append("\n\tRESOURCE_PATH: ");
		sb.append(resourcePath == null ? "null" : resourcePath.getAbsolutePath());
		sb.append("\n\tSCRIPT_LIB: ");
		sb.append(scriptLib == null ? "null" : scriptLib.getAbsolutePath());
		return sb.toString();
	}

	public IReportEngine getReportEngine() throws IOException, BirtException {
		final EngineConfig config = new EngineConfig();
		if (birtRuntimeHome != null) {
			config.setEngineHome(birtRuntimeHome.getAbsolutePath());
		}
		if (resourcePath != null) {
			config.setResourcePath(resourcePath.getAbsolutePath());
		}
		final String scriptlibFileNames = getScriptLibFileNames();
		if (scriptlibFileNames != null) {
			config.setProperty(EngineConstants.WEBAPP_CLASSPATH_KEY, scriptlibFileNames);
		}
		InputStream loggingPropertiesInputStream;
		if (loggingPropertiesFile == null) {
			loggingPropertiesInputStream = this.getClass().getResourceAsStream(
				"logging.properties");
		}
		else {
			final File loggingProperties = new File(loggingPropertiesFile);
			loggingPropertiesInputStream = new FileInputStream(loggingProperties);
		}
		LogManager.getLogManager().readConfiguration(loggingPropertiesInputStream);
		final Logger rootLogger = Logger.getLogger("");
		final Handler[] handlers = rootLogger.getHandlers();
		for (final Handler handler : handlers) {
			handler.setFormatter(new BatchFormatter());
		}
		// control debug of BIRT components.
		final File loggingDirFile = new File(loggingDir == null ? "/tmp/log" : loggingDir);
		if (!loggingDirFile.exists()) {
			loggingDirFile.mkdirs();
		}
		config.setLogConfig(loggingDirFile.getAbsolutePath(), Level.WARNING);
		return getReportEngine(config);
	}

	public static IReportEngine getReportEngine(final EngineConfig config)
			throws IOException, BirtException {
		Platform.startup(config);
		final IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(
			IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
		if (factory == null) {
			throw new NullPointerException("Could not create report engine factory");
		}
		final IReportEngine engine = factory.createReportEngine(config);
		return engine;
	}

	public IDesignEngine getDesignEngine() throws BirtException {
		final DesignConfig designConfig = new DesignConfig();
		designConfig.setBIRTHome(birtRuntimeHome.getAbsolutePath());
		Platform.startup(designConfig);
		final IDesignEngineFactory dfactory = (IDesignEngineFactory) Platform.createFactoryObject(
			IDesignEngineFactory.EXTENSION_DESIGN_ENGINE_FACTORY);
		final IDesignEngine designEngine = dfactory.createDesignEngine(designConfig);
		return designEngine;
	}

	/*
	 * The engine needs to see a list of each jar file concatenated as a string
	 * using the standard file system separator to divide the files
	 */
	private String getScriptLibFileNames() {
		if (scriptLib == null) {
			return null;
		}
		if (!scriptLib.exists()) {
			scriptLib.mkdirs();
		}
		final File[] dirFile = scriptLib.listFiles(new JarFilter());
		final StringBuffer sb = new StringBuffer(); //$NON-NLS-1$
		String sep = "";
		final String fileSeparatorString = new String(new char[] { File.pathSeparatorChar });
		for (int i = 0; i < dirFile.length; i++) {
			sb.append(sep);
			sep = fileSeparatorString;
			sb.append(dirFile[i].getAbsolutePath());
		}
		return sb.toString();
	}

	private static class JarFilter implements FilenameFilter {
		private final String extension = ".jar";

		@Override
		public boolean accept(final File dir, final String name) {
			return name.toLowerCase().endsWith(extension);
		}
	}
}
