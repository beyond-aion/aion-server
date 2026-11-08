package com.aionemu.chatserver.network.aion.clientpackets;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.main.LoggingConfig;
import com.aionemu.chatserver.dao.ChatLogDAO;
import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.model.channel.ChatChannels;
import com.aionemu.chatserver.model.message.Message;
import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.AionConnection;
import com.aionemu.chatserver.network.aion.serverpackets.SM_CHANNEL_MESSAGE;
import com.aionemu.chatserver.service.BroadcastService;

import io.netty.buffer.ByteBuf;

/**
 * @author ATracer
 */
public class CM_CHANNEL_MESSAGE extends AbstractClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_CHANNEL_MESSAGE.class);
	/**
	 * The 4.8 client sends at most 400 characters ({@code ccChannelChat} rejects longer texts).
	 */
	private static final int MAX_TEXT_LENGTH = 400;

	private int channelId;
	private byte[] content;

	public CM_CHANNEL_MESSAGE(ByteBuf buf, AionConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		readB(16); // user ID
		channelId = readD();
		readC(); // message type
		int contentLength = readUH() * 2;
		content = readB(contentLength);
	}

	@Override
	protected void runImpl() {
		ChatClient client = getConnection().getChatClient();
		if (content.length > MAX_TEXT_LENGTH * 2) {
			log.warn("{} sent a message of {} characters, the client allows {}, disconnecting", client, content.length / 2, MAX_TEXT_LENGTH);
			getConnection().close();
			return;
		}
		Channel channel = ChatChannels.getChannelById(channelId);
		if (channel == null)
			return;
		Message message = new Message(channel, content, client);
		if (client.isGagged()) {
			long gagTimeMin = (client.getGagTime() - System.currentTimeMillis()) / 1000 / 60;
			message.setText("You have been gagged for " + gagTimeMin + " minutes.");
			getConnection().sendPacket(new SM_CHANNEL_MESSAGE(message));
			return;
		}
		int floodProtectionTime = client.nextMessageTimeSec(channel.getChannelType());
		if (floodProtectionTime > 0) {
			message.setText("You can chat again in this channel in " + floodProtectionTime + " second" + (floodProtectionTime == 1 ? "." : "s."));
			getConnection().sendPacket(new SM_CHANNEL_MESSAGE(message));
			return;
		}
		client.updateLastMessageTime(channel.getChannelType());
		BroadcastService.getInstance().broadcastMessage(message);

		if (LoggingConfig.LOG_CHAT)
			LoggerFactory.getLogger("CHAT_LOG").info("[{}] {}: {}", message.getChannel().name(), message.getSender().getName(), message.getTextString());

		if (LoggingConfig.LOG_CHAT_TO_DB)
			ChatLogDAO.save(message.getSender().getName(), message.getTextString(), message.getChannel().name());
	}

	@Override
	public String toString() {
		return "CM_CHANNEL_MESSAGE [channelId=" + channelId + ", content=" + Arrays.toString(content) + "]";
	}
}
