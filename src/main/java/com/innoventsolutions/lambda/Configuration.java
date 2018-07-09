package com.innoventsolutions.lambda;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Configuration {
	public static class Editor {
		String workspace = null;
		String birtRuntimeHome = null;
		String resourcePath = null;
		String scriptLib = null;
		boolean doNotRun = false;
		String reportFormat = null;
		String baseImageURL = null;
		String loggingPropertiesFile = null;
		String loggingDir = null;
		int maxFailures = 5;
		String dataFilterDesign = null;
		String dataFilterOutput = null;
		String dataFilterParams = null;
		int threadCount = 1;

		public void loadProperties(final Properties properties) {
			final PropertiesHelper ph = new PropertiesHelper(properties);
			workspace = ph.get("birt.runner.workspace", workspace);
			birtRuntimeHome = ph.get("birt.runner.runtime", birtRuntimeHome);
			resourcePath = ph.get("birt.runner.resources", resourcePath);
			scriptLib = ph.get("birt.runner.scriptlib", scriptLib);
			reportFormat = ph.get("birt.runner.reportFormat", reportFormat);
			baseImageURL = ph.get("birt.runner.baseImageURL", baseImageURL);
			loggingPropertiesFile = ph.get("birt.runner.logging.properties", loggingPropertiesFile);
			loggingDir = ph.get("birt.runner.logging.dir", loggingDir);
			maxFailures = ph.get("birt.runner.max.failures", maxFailures);
			dataFilterDesign = ph.get("birt.runner.data.filter.design", dataFilterDesign);
			dataFilterOutput = ph.get("birt.runner.data.filter.output", dataFilterOutput);
			dataFilterParams = ph.get("birt.runner.data.filter.params", dataFilterParams);
			threadCount = ph.get("birt.runner.threadCount", threadCount);
		}

		public void loadArgs(final int offset, final String[] args) {
			int i = offset;
			while (i < args.length) {
				final String arg = args[i++];
				if (arg.startsWith("-")) {
					final String option = arg.substring(1).toUpperCase();
					final Processor processor = MAP.get(option);
					if (processor == null) {
						throw new IllegalArgumentException("Unrecognized option: " + option);
					}
					i = processor.process(i, args, this);
				}
				else {
				}
			}
		}
	}

	final String workspace;
	final String birtRuntimeHome;
	final String resourcePath;
	final String scriptLib;
	final boolean doNotRun;
	final String reportFormat;
	final String baseImageURL;
	final String loggingPropertiesFile;
	final String loggingDir;
	final int maxFailures;
	final String dataFilterDesign;
	final String dataFilterOutput;
	final String dataFilterParams;
	final int threadCount;

	public Configuration(final Editor editor) {
		this.workspace = editor.workspace;
		this.birtRuntimeHome = editor.birtRuntimeHome;
		this.resourcePath = editor.resourcePath;
		this.scriptLib = editor.scriptLib;
		this.doNotRun = editor.doNotRun;
		this.reportFormat = editor.reportFormat;
		this.baseImageURL = editor.baseImageURL;
		this.loggingPropertiesFile = editor.loggingPropertiesFile;
		this.loggingDir = editor.loggingDir;
		this.maxFailures = editor.maxFailures;
		this.dataFilterDesign = editor.dataFilterDesign;
		this.dataFilterOutput = editor.dataFilterOutput;
		this.dataFilterParams = editor.dataFilterParams;
		this.threadCount = editor.threadCount;
	}

	interface Processor {
		int process(int i, String[] args, Configuration.Editor configEditor);

		String getHelp();

		String getDataType();
	}

	private static Map<String, Processor> MAP;
	static {
		MAP = new HashMap<String, Processor>();
		MAP.put("W", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.workspace = args[i];
				return i + 1;
			}

			@Override
			public String getHelp() {
				return "Specify the default directory for report design files";
			}

			@Override
			public String getDataType() {
				return "String";
			}
		});
		MAP.put("B", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.birtRuntimeHome = args[i];
				return i + 1;
			}

			@Override
			public String getHelp() {
				return null;
			}

			@Override
			public String getDataType() {
				return null;
			}
		});
		MAP.put("R", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.resourcePath = args[i];
				return i + 1;
			}

			@Override
			public String getHelp() {
				return "Specify directory for report resources";
			}

			@Override
			public String getDataType() {
				return "String";
			}
		});
		MAP.put("S", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.scriptLib = args[i];
				return i + 1;
			}

			@Override
			public String getHelp() {
				return "Specify the directory containing event handler jar files";
			}

			@Override
			public String getDataType() {
				return "String";
			}
		});
		MAP.put("F", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.reportFormat = args[i];
				return i + 1;
			}

			@Override
			public String getHelp() {
				return "Specify the overall default report format (PDF, XLS, HTML, DOC, etc.)";
			}

			@Override
			public String getDataType() {
				return "String";
			}
		});
		MAP.put("I", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.baseImageURL = args[i];
				return i + 1;
			}

			@Override
			public String getHelp() {
				return "Specify the base image URL";
			}

			@Override
			public String getDataType() {
				return "String";
			}
		});
		MAP.put("LP", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.loggingPropertiesFile = args[i];
				return i + 1;
			}

			@Override
			public String getHelp() {
				return "Specify the logging properties file";
			}

			@Override
			public String getDataType() {
				return "String";
			}
		});
		MAP.put("LD", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.loggingDir = args[i];
				return i + 1;
			}

			@Override
			public String getHelp() {
				return "Specify the directory for logging files";
			}

			@Override
			public String getDataType() {
				return "String";
			}
		});
		MAP.put("FM", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.maxFailures = Integer.parseInt(args[i]);
				return i + 1;
			}

			@Override
			public String getHelp() {
				return "Specify the maximum number of report failures allowed";
			}

			@Override
			public String getDataType() {
				return "Integer";
			}
		});
		MAP.put("TC", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.threadCount = Integer.parseInt(args[i]);
				return i + 1;
			}

			@Override
			public String getHelp() {
				return "Specify the number of concurrent threads to run.  0 means one thread per report";
			}

			@Override
			public String getDataType() {
				return "Integer";
			}
		});
		MAP.put("DFD", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.dataFilterDesign = args[i];
				return i + 1;
			}

			@Override
			public String getHelp() {
				return "Specify the regular expression filter for design file name";
			}

			@Override
			public String getDataType() {
				return "String";
			}
		});
		MAP.put("DFO", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.dataFilterOutput = args[i];
				return i + 1;
			}

			@Override
			public String getHelp() {
				return "Specify the regular expression filter for output file name";
			}

			@Override
			public String getDataType() {
				return "String";
			}
		});
		MAP.put("DFP", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.dataFilterParams = args[i];
				return i + 1;
			}

			@Override
			public String getHelp() {
				return "Specify the regular expression filter for parameters";
			}

			@Override
			public String getDataType() {
				return "String";
			}
		});
		MAP.put("C", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				final String filename = args[i];
				final File file = new File(filename);
				final Properties properties = new Properties();
				try {
					final InputStream is = new FileInputStream(file);
					try {
						properties.load(is);
					}
					finally {
						is.close();
					}
				}
				catch (final FileNotFoundException e) {
					throw new IllegalArgumentException("Configuration file not found", e);
				}
				catch (final IOException e) {
					throw new IllegalArgumentException(e);
				}
				configEditor.loadProperties(properties);
				return i + 1;
			}

			@Override
			public String getHelp() {
				return "Specify a properties file for these options";
			}

			@Override
			public String getDataType() {
				return "String";
			}
		});
		MAP.put("H", new Processor() {
			@Override
			public int process(final int i, final String[] args,
					final Configuration.Editor configEditor) {
				configEditor.doNotRun = true;
				usage();
				return i;
			}

			@Override
			public String getHelp() {
				return "Print this message";
			}

			@Override
			public String getDataType() {
				return null;
			}
		});
	}

	public static void usage() {
		System.out.println("Usage java -cp <jarfiles> com.ingenico.birt.Main [options]");
		System.out.println("Options (case insensitive):");
		final List<String> keys = new ArrayList<>();
		for (final String key : MAP.keySet()) {
			keys.add(key);
		}
		Collections.sort(keys);
		for (final String key : keys) {
			final Processor processor = MAP.get(key);
			final String help = processor.getHelp();
			final String dataType = processor.getDataType();
			if (help != null) {
				final StringBuilder sb = new StringBuilder();
				sb.append(" -").append(key);
				if (dataType != null) {
					sb.append(" <").append(dataType).append(">");
				}
				sb.append(" ").append(help);
				System.out.println(sb);
			}
		}
	}
}
