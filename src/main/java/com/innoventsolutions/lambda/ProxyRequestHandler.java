package com.innoventsolutions.lambda;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class ProxyRequestHandler extends BaseRequestHandler
		implements RequestHandler<GatewayRequest, GatewayResponse> {
	@Override
	public GatewayResponse handleRequest(final GatewayRequest request, final Context context) {
		Map<String, String> parameters = request.getQueryStringParameters();
		if (parameters == null) {
			parameters = request.getPathParameters();
		}
		if (parameters == null) {
			final Map<String, String> headers = new HashMap<>();
			final GatewayResponse response = new GatewayResponse(false, 400, headers,
					"Missing required parameters");
			return response;
		}
		System.out.println("parameters = " + parameters);
		final ReportRunRequest rrRequest = new ReportRunRequest();
		rrRequest.setDesignUrl(parameters.remove("__design_url"));
		rrRequest.setFormat(parameters.remove("__format"));
		rrRequest.setOutputBucket(parameters.remove("__output_bucket"));
		rrRequest.setOutputKey(parameters.remove("__output_key"));
		final String runThenRender = parameters.remove("__run_then_render");
		if (runThenRender != null) {
			rrRequest.setRunThenRender("true".equals(runThenRender));
		}
		final String resources = parameters.remove("__resources");
		final String[] resourcesArray = resources == null ? null : resources.split(", *");
		final Map<String, String> resourcesMap = new HashMap<>();
		for (final String resource : resourcesArray) {
			final int indexOfEq = resource.indexOf("=");
			if (indexOfEq > 0) {
				final String path = resource.substring(0, indexOfEq);
				final String url = resource.substring(indexOfEq + 1);
				resourcesMap.put(path, url);
			}
		}
		rrRequest.setResources(resourcesMap);
		rrRequest.setParameters(parameters);
		final ReportRunResponse rrResponse = super.handleRequest(rrRequest);
		final Map<String, String> headers = new HashMap<>();
		String message = rrResponse.getMessage();
		if (message != null) {
			message = message.replace("\"", "\\\"");
		}
		final GatewayResponse response = new GatewayResponse(false,
				rrResponse.isSuccess() ? 200 : 500, headers, message);
		return response;
	}
}
