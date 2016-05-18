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

/**
 * Based on the work of <a href="https://github.com/anjlab/logback-hipchat-appender">Dmitry Gusev</a>
 * 
 * <p>Reference for possible object content: <a href="https://www.hipchat.com/docs/apiv2/method/send_room_notification">Link</a></p>
 * 
 * @author Dmitry Gusev
 * @modified Neon
 */
public class HipChatMessage {

	public static final int MAX_MESSAGE_LENGTH = 10000;

	public final String from; // A label to be shown in addition to the sender's name
	public final String message;
	public final String message_format; // null = hipchat api default (html), currently supported: html, text
	public final String color; // null = hipchat api default (yellow), currently supported: yellow, red, green, purple, gray, random
	public final Boolean notify; // null = hipchat api default (false)

	public HipChatMessage(String message) {
		this(null, message, null, null, null);
	}

	public HipChatMessage(String from, String message, String messageFormat, String color, Boolean notify) {
		this.from = from;
		this.message = message;
		this.message_format = messageFormat;
		this.color = color;
		this.notify = notify;
	}
}
