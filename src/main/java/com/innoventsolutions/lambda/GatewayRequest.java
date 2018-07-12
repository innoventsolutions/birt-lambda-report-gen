package com.innoventsolutions.lambda;

import java.util.Map;

public class GatewayRequest {
	private String resource;
	private String path;
	private String httpMethod;
	private Object headers;
	private Map<String, String> queryStringParameters;
	private Map<String, String> pathParameters;
	private Object stateVariables;
	private Object requestContext;
	private String body;
	private boolean isBase64Encoded;

	public String getResource() {
		return resource;
	}

	public void setResource(final String resource) {
		this.resource = resource;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(final String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public Object getHeaders() {
		return headers;
	}

	public void setHeaders(final Object headers) {
		this.headers = headers;
	}

	public Map<String, String> getQueryStringParameters() {
		return queryStringParameters;
	}

	public void setQueryStringParameters(final Map<String, String> queryStringParameters) {
		this.queryStringParameters = queryStringParameters;
	}

	public Map<String, String> getPathParameters() {
		return pathParameters;
	}

	public void setPathParameters(final Map<String, String> pathParameters) {
		this.pathParameters = pathParameters;
	}

	public Object getStateVariables() {
		return stateVariables;
	}

	public void setStateVariables(final Object stateVariables) {
		this.stateVariables = stateVariables;
	}

	public Object getRequestContext() {
		return requestContext;
	}

	public void setRequestContext(final Object requestContext) {
		this.requestContext = requestContext;
	}

	public String getBody() {
		return body;
	}

	public void setBody(final String body) {
		this.body = body;
	}

	public boolean isBase64Encoded() {
		return isBase64Encoded;
	}

	public void setBase64Encoded(final boolean isBase64Encoded) {
		this.isBase64Encoded = isBase64Encoded;
	}
}
