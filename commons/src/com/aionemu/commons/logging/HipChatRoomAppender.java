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

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;

import com.aionemu.commons.logging.LineChunkenizer.ChunkCallback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

/**
 * Based on the work of <a href="https://github.com/anjlab/logback-hipchat-appender">Dmitry Gusev</a>
 * 
 * @author Dmitry Gusev
 * @modified Neon
 */
public class HipChatRoomAppender<E> extends AppenderBase<E> {

	private Layout<E> layout; // required
	private String room; // required
	private String apiKey; // required
	private String color; // null = hipchat api default (yellow), level = colored based on logging level
	private Boolean notify; // null = hipchat api default (false)
	private String messageFormat; // null = hipchat api default (html), currently supported: html, text
	private String senderSeparatorPattern; // text in front of this pattern (if not null), will be set as a sender label

	private HipChatRoom hipChatRoom;

	@Override
	public void start() {
		boolean hasErrors = guardNotNull(layout, "Layout") || guardNotNull(room, "HipChat room") || guardNotNull(apiKey, "HipChat room API key");

		if (hasErrors) {
			return;
		}

		hipChatRoom = new HipChatRoom(room, apiKey);
		super.start();
	}

	private boolean guardNotNull(Object value, String name) {
		if (value == null || value instanceof String && (((String) value).isEmpty() || ((String) value).endsWith("_IS_UNDEFINED"))) {
			addError(name + " == null or not specified, but is mandatory");
			return true;
		}
		return false;
	}

	@Override
	public void stop() {
		super.stop();
		IOUtils.closeQuietly(hipChatRoom);
	}

	private final AtomicLong eventCounter = new AtomicLong();

	@Override
	protected void append(E eventObject) {
		final long eventId = eventCounter.incrementAndGet();

		String rawMessage = layout.doLayout(eventObject);
		String from = null;
		if (senderSeparatorPattern != null) {
			String[] parts = rawMessage.split(senderSeparatorPattern, 2);
			if (parts.length > 1) {
				from = parts[0];
				rawMessage = parts[1];
			}
		}

		int maxWrapperLength = 20;
		final int maxChunkSize = HipChatMessage.MAX_MESSAGE_LENGTH - maxWrapperLength;
		final String senderName = from;
		final String color = selectColor(this.color, eventObject);

		new LineChunkenizer(rawMessage, maxChunkSize).chunkenize(new ChunkCallback() {

			private int chunkId = 0;

			@Override
			public void gotChunk(String chunk, boolean hasMoreChunks) {
				chunkId++;

				StringBuilder message = new StringBuilder(hasMoreChunks ? HipChatMessage.MAX_MESSAGE_LENGTH : chunk.length());
				if (hasMoreChunks || chunkId > 1) {
					message.append("[").append(eventId).append(':').append(chunkId).append(hasMoreChunks ? "]+more\n" : "]-last\n");
				}
				message.append(chunk);

				hipChatRoom.sendMessage(new HipChatMessage(senderName, message.toString(), messageFormat, color, notify));
			}
		});
	}

	public void setLayout(Layout<E> layout) {
		this.layout = layout;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public void setMessageFormat(String messageFormat) {
		this.messageFormat = messageFormat;
	}

	public void setSenderSeparatorPattern(String senderSeparatorPattern) {
		this.senderSeparatorPattern = senderSeparatorPattern;
	}

	private String selectColor(String color, E eventObject) {
		if (!"level".equalsIgnoreCase(color))
			return color;

		switch (((ILoggingEvent) eventObject).getLevel().levelStr) {
			case "INFO":
				return "purple";
			case "WARN":
				return "yellow";
			case "ERROR":
				return "red";
			default:
				return "gray";
		}
	}
}
