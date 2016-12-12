package com.aionemu.loginserver.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.model.ExternalAuth;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * @author Woge, Neon
 */
public class ExternalAuthUtil {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(ExternalAuthUtil.class);

	/**
	 * Returns an ExternalAuth object holding the information how and if the user authenticated.
	 * 
	 * @param name
	 * @param password
	 * @return ExternalAuth object or null
	 */
	public static synchronized ExternalAuth requestInfo(String name, String password) {

		ExternalAuth info = null;
		String charset = StandardCharsets.UTF_8.name();
		String query = "";
		BufferedReader reader = null;
		JsonParser parser = new JsonParser();

		try {
			query = String.format("u=%s&p=%s", URLEncoder.encode(name, charset), URLEncoder.encode(password, charset));

			HttpURLConnection connection = (HttpURLConnection) new URL(Config.AUTH_EXTERNAL_JSON_URL).openConnection();
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setFixedLengthStreamingMode(query.getBytes(charset).length);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
			connection.setRequestProperty("User-Agent", "AionLS");

			// send query to auth server
			connection.getOutputStream().write(query.getBytes(charset));
			connection.getOutputStream().close();

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				// read response from auth server
				String result = "";
				for (String line; (line = reader.readLine()) != null;) {
					result += line;
				}
				reader.close();
				connection.getInputStream().close();

				if (result.length() > 0) {
					info = new ExternalAuth();
					JsonObject answer = (JsonObject) parser.parse(result);

					if (answer.has("id")) {
						String id = answer.get("id").toString();
						info.setIdentifier(id);
					}

					if (answer.has("state")) {
						try {
							int state = Integer.parseInt(answer.get("state").toString());
							info.setAuthState(state);
						} catch (NumberFormatException e) {
							log.error(ExternalAuthUtil.class.getSimpleName() + ": error parsing auth state - " + e.getMessage());
						}
					} else {
						log.error(ExternalAuthUtil.class.getSimpleName() + ": no auth state received from " + Config.AUTH_EXTERNAL_JSON_URL);
					}
				} else {
					log.error(ExternalAuthUtil.class.getSimpleName() + ": empty response from " + Config.AUTH_EXTERNAL_JSON_URL);
				}
			} else {
				log.error(ExternalAuthUtil.class.getSimpleName() + ": " + Config.AUTH_EXTERNAL_JSON_URL + " returned response code " + responseCode);
			}
		} catch (UnsupportedEncodingException | IllegalFormatException e) {
			log.error(ExternalAuthUtil.class.getSimpleName() + ": error generating query string - " + e.getMessage());
		} catch (IOException | JsonParseException | ClassCastException e) {
			log.error(ExternalAuthUtil.class.getSimpleName() + ": " + e.toString());
		}

		return info;
	}
}
