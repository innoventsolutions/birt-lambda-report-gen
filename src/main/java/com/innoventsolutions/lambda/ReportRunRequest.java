package com.innoventsolutions.lambda;

import java.util.Map;

public class ReportRunRequest {
	private String designUrl;
	private String outputBucket;
	private String outputKey;
	private Map<String, String> parameters;
	private String format;
	private boolean runThenRender;
	private boolean runThenRenderSet = false;
	private Map<String, String> resources;

	public String getFormat() {
		return format;
	}

	public void setFormat(final String format) {
		this.format = format;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(final Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getDesignUrl() {
		return designUrl;
	}

	public void setDesignUrl(final String designUrl) {
		this.designUrl = designUrl;
	}

	public boolean isRunThenRender() {
		return runThenRender;
	}

	public void setRunThenRender(final boolean runThenRender) {
		this.runThenRender = runThenRender;
		runThenRenderSet = true;
	}

	public String getOutputBucket() {
		return outputBucket;
	}

	public void setOutputBucket(final String outputBucket) {
		this.outputBucket = outputBucket;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(final String outputKey) {
		this.outputKey = outputKey;
	}

	public boolean isRunThenRenderSet() {
		return runThenRenderSet;
	}

	public void setRunThenRenderSet(final boolean runThenRenderSet) {
		this.runThenRenderSet = runThenRenderSet;
	}

	public Map<String, String> getResources() {
		return resources;
	}

	public void setResources(final Map<String, String> resources) {
		this.resources = resources;
	}
}
