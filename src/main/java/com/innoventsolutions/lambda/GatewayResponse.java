package com.innoventsolutions.lambda;

import java.util.HashMap;
import java.util.Map;

public class GatewayResponse {
	public final boolean isBase64Encoded;
	public final int statusCode;
	public final Map<String, String> headers;
	public final String body;

	public GatewayResponse(final boolean isBase64Encoded, final int statusCode,
			final Map<String, String> headers, final String body) {
		this.isBase64Encoded = isBase64Encoded;
		this.statusCode = statusCode;
		this.headers = new HashMap<>(headers);
		this.body = body;
	}
}
