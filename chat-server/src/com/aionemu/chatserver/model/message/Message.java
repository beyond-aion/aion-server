package com.aionemu.chatserver.model.message;

import java.nio.charset.StandardCharsets;

import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.channel.Channel;

/**
 * @author ATracer
 */
public class Message {

	private final Channel channel;
	private final ChatClient sender;
	private byte[] text;

	public Message(Channel channel, byte[] text, ChatClient sender) {
		this.channel = channel;
		this.sender = sender;
		this.text = text;
	}

	public Channel getChannel() {
		return channel;
	}

	public byte[] getText() {
		return text;
	}

	public void setText(String str) {
		this.text = str.getBytes(StandardCharsets.UTF_16LE);
	}

	public int size() {
		return text.length;
	}

	public ChatClient getSender() {
		return sender;
	}

	public String getTextString() {
		return new String(text, StandardCharsets.UTF_16LE);
	}
}
