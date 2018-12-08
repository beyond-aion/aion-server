package com.aionemu.loginserver.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.IllegalFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.model.ExternalAuth;
import com.google.gson.Gson;

/**
 * @author Woge, Neon
 */
public class ExternalAuthUtil {

	private static final Logger log = LoggerFactory.getLogger(ExternalAuthUtil.class);
	private static final URL url;
	private static final Gson gson = new Gson();

	static {
		try {
			url = new URL(Config.AUTH_EXTERNAL_JSON_URL);
		} catch (MalformedURLException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	/**
	 * Returns an ExternalAuth object holding the information how and if the user authenticated.
	 * 
	 * @param name
	 * @param password
	 * @return ExternalAuth object or null on connection error
	 */
	public static ExternalAuth requestInfo(String name, String password) {
		ExternalAuth info = null;
		try {
			String query = String.format("u=%s&p=%s", URLEncoder.encode(name, UTF_8.name()), URLEncoder.encode(password, UTF_8.name())); // strange query format due to php endpoint
			byte[] payload = query.getBytes(UTF_8);

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setFixedLengthStreamingMode(payload.length);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + UTF_8);
			con.setRequestProperty("User-Agent", "AionLS");

			// send query to auth server
			try (OutputStream os = con.getOutputStream()) {
				os.write(payload);
			}

			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				try (Reader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), UTF_8))) {
					info = gson.fromJson(reader, ExternalAuth.class);
				}
			} else {
				log.error(url + " returned response code " + con.getResponseCode());
			}
		} catch (UnsupportedEncodingException | IllegalFormatException e) {
			log.error("Error generating query string for user: " + name + ", pw: " + password.replace('.', '*'), e);
		} catch (Exception e) {
			log.error(null, e);
		}

		return info;
	}
}
