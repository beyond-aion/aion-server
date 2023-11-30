package com.aionemu.loginserver.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.configs.Config;
import com.alibaba.fastjson2.JSON;

/**
 * @author Neon
 */
public class ExternalAuth {

	private static final Logger log = LoggerFactory.getLogger(ExternalAuth.class);
	private static final URI uri = URI.create(Config.EXTERNAL_AUTH_URL);
	private static final HttpClient httpClient = HttpClient.newHttpClient();

	public static Response authenticate(String user, String password) {
		Response info = null;
		try {
			HttpRequest httpRequest = HttpRequest.newBuilder(uri)
					.headers("User-Agent", "AionLS")
					.headers("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(Map.of("user", user, "password", password))))
					.build();
			HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				info = JSON.parseObject(response.body(), Response.class);
			} else {
				log.warn("Server returned status code " + response.statusCode() + (response.body().isEmpty() ? "" : ": " + response.body()));
			}
		} catch (InterruptedException ignored) {
		} catch (Exception e) {
			log.error("Could not login user " + user, e);
		}
		return info;
	}

	public record Response(String accountId, int aionAuthResponseId) {}
}
