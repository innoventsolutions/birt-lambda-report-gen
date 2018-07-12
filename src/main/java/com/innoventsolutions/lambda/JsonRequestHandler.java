package com.innoventsolutions.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class JsonRequestHandler extends BaseRequestHandler
		implements RequestHandler<ReportRunRequest, ReportRunResponse> {
	@Override
	public ReportRunResponse handleRequest(final ReportRunRequest input, final Context context) {
		return super.handleRequest(input);
	}
}
