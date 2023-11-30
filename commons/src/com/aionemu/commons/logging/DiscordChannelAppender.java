package com.aionemu.commons.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.Encoder;

/**
 * @author Neon
 */
public class DiscordChannelAppender<E> extends AppenderBase<E> {

	private static final Logger log = LoggerFactory.getLogger(DiscordChannelAppender.class);
	private static final int MAX_USERNAME_LENGTH = 32;
	private static final int MAX_MESSAGE_LENGTH = 2000;
	private static final Pattern CODE_BLOCK_TYPE_PATTERN = Pattern.compile("(```(?:[a-z]+\r?\n)?)");
	private static final String CODE_BLOCK_END = "```";
	private final Gson gson = new Gson();
	private final AtomicLong floodResetTimeMillis = new AtomicLong();
	private Encoder<E> encoder; // required
	private String webhookUrl; // required
	private String userName_avatarUrl_msg_separator; // if specified, extracts user name and avatar to use by splitting the message with the separator
	private URL webhook;

	@Override
	public void start() {
		if (checkValueMissing(encoder, "<encoder>") || checkValueMissing(webhookUrl, "<webhookUrl>")) {
			return;
		}
		if (webhookUrl.isEmpty()) {
			addInfo("<webhookUrl> is empty, appender will not be used");
		} else {
			try {
				webhook = new URL(webhookUrl);
			} catch (MalformedURLException e) {
				addError("Invalid Webhook Url", e);
				return;
			}
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
			sendMessage(userName, avatarUrl, messagePart);
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
				String codeBlockStarPlusNewLine = codeBlockStart + '\n';
				if (sb.lastIndexOf(codeBlockStarPlusNewLine) == sb.length() - codeBlockStarPlusNewLine.length()) {
					// don't generate an empty code block at the end of the string
					sb.setLength(sb.length() - codeBlockStarPlusNewLine.length());
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

	private void sendMessage(String userName, String avatarUrl, String msg) {
		if (!canSend())
			return;

		Map<String, Object> message = new LinkedHashMap<>(3);
		message.put("content", msg);
		message.put("username", userName);
		message.put("avatar_url", avatarUrl);

		try {
			String json = gson.toJson(message);
			byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
			HttpURLConnection con = (HttpURLConnection) webhook.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setFixedLengthStreamingMode(bytes.length);
			con.setRequestProperty("User-Agent", "DiscordChannelAppender/1.0");
			con.setRequestProperty("Content-Type", "application/json");
			con.connect();
			try (OutputStream os = con.getOutputStream()) {
				os.write(bytes);
			}
			if (con.getResponseCode() == 429) {
				long resetTime = 0;
				long now = System.currentTimeMillis();
				String ratelimitDurationMillis = con.getHeaderField("Retry-After");
				if (ratelimitDurationMillis != null && ratelimitDurationMillis.matches("\\d+")) {
					resetTime = now + Long.parseLong(ratelimitDurationMillis);
				} else {
					String ratelimitResetTime = con.getHeaderField("X-Ratelimit-Reset");
					if (ratelimitResetTime != null && ratelimitResetTime.matches("\\d+")) {
						resetTime = Long.parseLong(ratelimitResetTime) * 1000;
					}
				}
				floodResetTimeMillis.set(resetTime > now ? resetTime : now + 3000);
				throw new ConnectException("Flood control for channel triggered, reset in " + (floodResetTimeMillis.get() - now) / 1000
					+ "s. Meanwhile, all messages will be dropped.");
			} else if (con.getResponseCode() >= 300) {
				throw new ConnectException("HTTP Response " + con.getResponseCode() + ": " + con.getResponseMessage());
			}
		} catch (IOException e) {
			String errorHeader = "Error sending Discord message: ";
			if (!msg.contains(errorHeader)) // avoid potential recursive message sending (if appender sends warnings)
				log.warn(errorHeader + msg + "\nCaused by: " + e.getMessage());
		}
	}

	public boolean canSend() {
		long resetTime = floodResetTimeMillis.get();
		if (resetTime == 0)
			return true;
		if (resetTime > System.currentTimeMillis())
			return false;
		floodResetTimeMillis.set(0);
		return true;
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
