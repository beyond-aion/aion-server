package com.aionemu.commons.logging;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSON;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.status.StatusManager;

/**
 * Sends messages via a webhook (see <a href="https://discord.com/developers/docs/resources/webhook#execute-webhook">API docs</a>).<br>
 * If the message is longer than {@value #MAX_MESSAGE_LENGTH} characters (Discord limit) it will be sent in parts, while keeping code blocks intact.
 * 
 * @author Neon
 */
public class DiscordChannelAppender<E> extends AppenderBase<E> {

	private static final Logger log = LoggerFactory.getLogger(DiscordChannelAppender.class);
	private static final int MAX_USERNAME_LENGTH = 32;
	private static final int MAX_MESSAGE_LENGTH = 2000;
	private static final Pattern CODE_BLOCK_TYPE_PATTERN = Pattern.compile("(```(?:[a-z]+\r?\n)?)");
	private static final String CODE_BLOCK_END = "```";
	private final AtomicLong floodResetTimeMillis = new AtomicLong();
	private Encoder<E> encoder; // required
	private String webhookUrl; // required
	private String userName_avatarUrl_msg_separator; // if specified, extracts user name and avatar to use by splitting the message with the separator
	private URI webhookUri;
	private HttpClient httpClient;

	@Override
	public void start() {
		if (checkValueMissing(encoder, "<encoder>") || checkValueMissing(webhookUrl, "<webhookUrl>")) {
			return;
		}
		if (webhookUrl.isEmpty()) {
			addInfo("<webhookUrl> is empty, appender will not be used");
			StatusManager statusManager = context.getStatusManager();
			statusManager.getCopyOfStatusListenerList().forEach(statusManager::remove);
		} else {
			webhookUri = URI.create(webhookUrl);
			httpClient = HttpClient.newHttpClient();
			super.start();
		}
	}

	private boolean checkValueMissing(Object value, String name) {
		if (value == null) {
			addError(name + " is missing");
			return true;
		}
		if (value instanceof String v && v.endsWith(CoreConstants.UNDEFINED_PROPERTY_SUFFIX)) {
			addError(name + " is unresolved (configuration value for ${" + v.substring(0, v.length() - CoreConstants.UNDEFINED_PROPERTY_SUFFIX.length()) + "} is not set)");
			return true;
		}
		return false;
	}

	@Override
	public void stop() {
		if (httpClient != null) {
			httpClient.close();
			httpClient = null;
		}
		super.stop();
	}

	@Override
	protected void append(E eventObject) {
		String rawMessage = new String(encoder.encode(eventObject));
		String userName = null;
		String avatarUrl = null;
		String msg = rawMessage;
		if (userName_avatarUrl_msg_separator != null && !userName_avatarUrl_msg_separator.isEmpty()) {
			String[] parts = rawMessage.split(userName_avatarUrl_msg_separator, 3);
			for (int i = parts.length - 1, partCount = 0; i >= 0; i--, partCount++) {
				if (partCount == 0) {
					msg = parts[i];
				} else if (partCount == 1) {
					avatarUrl = parts[i].trim();
				} else if (partCount == 2) {
					userName = parts[i].trim();
				}
			}
		}
		if (userName != null && userName.length() > MAX_USERNAME_LENGTH)
			userName = userName.substring(0, MAX_USERNAME_LENGTH - 1) + '…';
		for (String messagePart : createMessageParts(msg)) {
			sendMessage(messagePart, userName, avatarUrl);
		}
	}

	private List<String> createMessageParts(String msg) {
		msg = msg.replaceAll("\r\n", "\n"); // try to slightly shrink message due to the low message length limit
		if (msg.length() <= MAX_MESSAGE_LENGTH)
			return Collections.singletonList(msg);
		List<String> messageParts = new ArrayList<>();
		String codeBlockStart = null;
		int codeStartIndex = Integer.MAX_VALUE;
		int codeEndIndex = -1;
		if (msg.trim().endsWith(CODE_BLOCK_END)) {
			Matcher matcher = CODE_BLOCK_TYPE_PATTERN.matcher(msg);
			if (matcher.find()) {
				codeBlockStart = matcher.group(1).replaceAll("\n", "");
				codeStartIndex = matcher.end() + 1;
				codeEndIndex = msg.lastIndexOf(CODE_BLOCK_END) - 1;
			}
		}
		int msgPosition = -1;
		String[] lines = msg.split("\n");
		StringBuilder sb = new StringBuilder(MAX_MESSAGE_LENGTH);
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			msgPosition += line.length() + (i == 0 ? 0 : 1);
			boolean isNewMessagePart = sb.length() == 0;
			boolean isInsideCodeBlock = msgPosition >= codeStartIndex && msgPosition <= codeEndIndex;
			if (isNewMessagePart && isInsideCodeBlock && !line.contains(codeBlockStart))
				sb.append(codeBlockStart).append('\n');
			else if (!isNewMessagePart)
				sb.append('\n');
			int overflowingChars = calcTotalLength(line, sb, isInsideCodeBlock) - MAX_MESSAGE_LENGTH;
			if (overflowingChars <= 0) { // fits into current messagePart
				sb.append(line);
				if (i < lines.length - 1)
					continue;
			} else if (isNewMessagePart) { // must be truncated
				if (isInsideCodeBlock != (isInsideCodeBlock = msgPosition - overflowingChars >= codeStartIndex
					&& msgPosition - overflowingChars <= codeEndIndex))
					overflowingChars = calcTotalLength(line, sb, isInsideCodeBlock) - MAX_MESSAGE_LENGTH;
				sb.append(line, 0, line.length() - overflowingChars - 1).append('…');
			}
			if (isInsideCodeBlock) {
				String codeBlockStartPlusNewLine = codeBlockStart + '\n';
				if (sb.lastIndexOf(codeBlockStartPlusNewLine) == sb.length() - codeBlockStartPlusNewLine.length()) {
					// don't generate an empty code block at the end of the string
					sb.setLength(sb.length() - codeBlockStartPlusNewLine.length());
					if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') // remove empty line
						sb.setLength(sb.length() - 1);
				} else
					sb.append(CODE_BLOCK_END);
			}
			if (sb.length() > 0 || line.length() == 0) // don't add an empty messagePart if it's because of a removed empty code block (see above)
				messageParts.add(sb.toString());
			sb.setLength(0);
			if (!isNewMessagePart && overflowingChars > 0) { // this line will be the start of a new messagePart
				msgPosition -= line.length() + (i == 0 ? 0 : 1); // avoid duplicate count
				i--;
			}
		}
		return messageParts;
	}

	private int calcTotalLength(String line, StringBuilder sb, boolean isInsideCodeBlock) {
		int length = line.length() + sb.length();
		if (isInsideCodeBlock)
			length += CODE_BLOCK_END.length();
		return length;
	}

	private void sendMessage(String msg, String userName, String avatarUrl) {
		if (isRateLimited())
			return;
		try {
			byte[] json = JSON.toJSONBytes(Map.of("content", msg, "username", userName, "avatar_url", avatarUrl));
			HttpRequest httpRequest = HttpRequest.newBuilder(webhookUri)
					.headers("User-Agent", "DiscordChannelAppender/1.0")
					.headers("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofByteArray(json))
					.build();
			HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			handleResponse(response);
		} catch (InterruptedException ignored) {
		} catch (Exception e) {
			String errorHeader = "Error sending Discord message: ";
			if (!msg.contains(errorHeader)) // avoid potential recursive message sending (if appender sends warnings)
				log.warn(errorHeader + msg + "\nCaused by: " + e.getMessage());
		}
	}

	private void handleResponse(HttpResponse<String> response) throws IOException {
		if (response.statusCode() == 429) {
			long resetTime;
			long now = System.currentTimeMillis();
			long rateLimitDurationMillis = response.headers().firstValueAsLong("Retry-After").orElse(0);
			if (rateLimitDurationMillis > 0) {
				resetTime = now + rateLimitDurationMillis;
			} else {
				resetTime = response.headers().firstValueAsLong("X-RateLimit-Reset").orElse(0) * 1000;
			}
			floodResetTimeMillis.set(resetTime > now ? resetTime : now + 3000);
			throw new IOException(
				"Flood control for channel triggered, reset in " + (floodResetTimeMillis.get() - now) / 1000 + "s. Meanwhile, all messages will be dropped.");
		} else if (response.statusCode() != 204) {
			throw new IOException("Server returned status code " + response.statusCode() + (response.body().isEmpty() ? "" : ": " + response.body()));
		}
	}

	private boolean isRateLimited() {
		return floodResetTimeMillis.get() > System.currentTimeMillis();
	}

	public void setEncoder(Encoder<E> encoder) {
		this.encoder = encoder;
	}

	public void setWebhookUrl(String webhookUrl) {
		this.webhookUrl = webhookUrl;
	}

	public void setUserName_avatarUrl_msg_separator(String userName_avatarUrl_msg_separator) {
		this.userName_avatarUrl_msg_separator = userName_avatarUrl_msg_separator;
	}
}
