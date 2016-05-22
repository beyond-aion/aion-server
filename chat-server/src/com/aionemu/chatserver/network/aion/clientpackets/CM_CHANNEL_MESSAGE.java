package com.aionemu.chatserver.network.aion.clientpackets;

import java.util.Arrays;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.main.LoggingConfig;
import com.aionemu.chatserver.dao.ChatLogDAO;
import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.model.channel.ChatChannels;
import com.aionemu.chatserver.model.message.Message;
import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.serverpackets.SM_CHANNEL_MESSAGE;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;
import com.aionemu.chatserver.service.BroadcastService;
import com.aionemu.commons.database.dao.DAOManager;

/**
 * @author ATracer
 */
public class CM_CHANNEL_MESSAGE extends AbstractClientPacket {

	private int channelId;
	private byte[] content;

	/**
	 * @param channelBuffer
	 * @param gameChannelHandler
	 * @param opCode
	 */
	public CM_CHANNEL_MESSAGE(ChannelBuffer channelBuffer, ClientChannelHandler gameChannelHandler) {
		super(channelBuffer, gameChannelHandler, 0x18);
	}

	@Override
	protected void readImpl() {
		readH();
		readC();
		readD();
		readD();
		readD();
		readD();
		channelId = readD();
		readC();
		int lenght = readH() * 2;
		content = readB(lenght);
	}

	@Override
	protected void runImpl() {
		Channel channel = ChatChannels.getChannelById(channelId);
		ChatClient client = clientChannelHandler.getChatClient();
		Message message = new Message(channel, content, client);
		if (client.isGagged()) {
			long endTime = (client.getGagTime() - System.currentTimeMillis()) / 1000 / 60;
			message.setText("You have been gagged for " + endTime + " minutes.");
			clientChannelHandler.sendPacket(new SM_CHANNEL_MESSAGE(message));
			return;
		}
		int floodProtectionTime = client.getFloodProtectionTime(channel.getChannelType());
		if (floodProtectionTime > 0) {
			message.setText("You can chat again in this channel in " + floodProtectionTime + " second" + (floodProtectionTime == 1 ? "." : "s."));
			clientChannelHandler.sendPacket(new SM_CHANNEL_MESSAGE(message));
			return;
		}
		client.updateLastMessageTime(channel.getChannelType());
		BroadcastService.getInstance().broadcastMessage(message);

		if (LoggingConfig.LOG_CHAT) {
			LoggerFactory.getLogger("CHAT_LOG").info("[{}] {}: {}", message.getChannel().name(), message.getSenderName(), message.getTextString());
		}

		if (LoggingConfig.LOG_CHAT_TO_DB) {
			DAOManager.getDAO(ChatLogDAO.class).add_ChannelChat(message.getSenderName(), message.getTextString(), "", message.getChannel().name());
		}
	}

	@Override
	public String toString() {
		return "CM_CHANNEL_MESSAGE [channelId=" + channelId + ", content=" + Arrays.toString(content) + "]";
	}
}
