/**
 * Copyright 2014 AnjLab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aionemu.commons.logging;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;

/**
 * Based on the work of <a href="https://github.com/anjlab/logback-hipchat-appender">Dmitry Gusev</a>
 * 
 * @author Dmitry Gusev
 * @modified Neon
 */
public class HipChatRoom implements Closeable {

	private static final Logger log = LoggerFactory.getLogger(HipChatRoom.class);
	private final CloseableHttpClient closeableHttpClient;
	private final Gson gson;

	private final String roomUri;
	private final Header authorizationHeader;
	private final Header contentTypeHeader;
	private AtomicLong floodResetTime = new AtomicLong(); // unix timestamp

	public HipChatRoom(String room, String apiKey) {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

		closeableHttpClient = HttpClients.custom().setConnectionManager(cm).build();
		gson = new Gson();
		roomUri = "https://api.hipchat.com/v2/room/" + room + "/notification";
		authorizationHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
		contentTypeHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
	}

	/**
	 * <a href="https://www.hipchat.com/docs/apiv2/method/send_room_notification">HipChat documentation</a>
	 * 
	 * @param message
	 */
	public void sendMessage(HipChatMessage message) {
		if (!canSend())
			return;
		String errorHeader = "Error sending HipChat message: ";
		if (message.message.startsWith(errorHeader))
			return;
		String json = gson.toJson(message);

		HttpPost request = new HttpPost(roomUri);
		request.addHeader(contentTypeHeader);
		request.addHeader(authorizationHeader);
		request.setEntity(new StringEntity(json, Charset.defaultCharset()));

		CloseableHttpResponse response = null;
		try {
			response = closeableHttpClient.execute(request);

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
				String errorMsg;
				if (response.getStatusLine().getStatusCode() == 429) { // too many requests
					long resetTime = 0;
					long now = System.currentTimeMillis() / 1000;
					Header header = response.getLastHeader("X-Ratelimit-Reset");
					if (header != null) {
						String time = header.getValue();
						if (time != null)
							resetTime = Long.valueOf(time);
					}
					floodResetTime.set(resetTime > now ? resetTime : now + 10);
					errorMsg = "Flood control for room triggered, reset in " + (floodResetTime.get() - now) + "s. Meanwhile, all messages will be dropped.";
				} else {
					errorMsg = response.getStatusLine().toString();
				}
				throw new HttpResponseException(response.getStatusLine().getStatusCode(), "HTTP status message: " + errorMsg);
			}
		} catch (IOException e) {
			log.warn(errorHeader + message.message + "\nCaused by: " + (e instanceof HttpResponseException ? e.getMessage() : e));
		} finally {
			IOUtils.closeQuietly(response);
		}
	}

	public boolean canSend() {
		long resetTime = floodResetTime.get();
		if (resetTime == 0)
			return true;
		if (resetTime > System.currentTimeMillis() / 1000)
			return false;
		floodResetTime.set(0);
		return true;
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	@Override
	public void close() throws IOException {
		closeableHttpClient.close();
	}
}
