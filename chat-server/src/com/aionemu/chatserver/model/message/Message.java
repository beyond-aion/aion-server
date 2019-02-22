package com.aionemu.chatserver.model.message;

import java.nio.charset.StandardCharsets;

import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.channel.Channel;

/**
 * @author ATracer
 */
public class Message {

	private Channel channel;
	private byte[] text;
	private ChatClient sender;

	/**
	 * @param channel
	 * @param text
	 */
	public Message(Channel channel, byte[] text, ChatClient sender) {
		this.channel = channel;
		this.text = text;
		this.sender = sender;
	}

	public void setText(String str) {
		this.text = str.getBytes(StandardCharsets.UTF_16LE);
	}

	/**
	 * @return the channel
	 */
	public Channel getChannel() {
		return channel;
	}

	/**
	 * @return the text
	 */
	public byte[] getText() {
		return text;
	}

	public int size() {
		return text.length;
	}

	/**
	 * @return the sender
	 */
	public ChatClient getSender() {
		return sender;
	}

	public String getTextString() {
		return new String(text, StandardCharsets.UTF_16LE);
	}
}
