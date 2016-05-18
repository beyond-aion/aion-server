package com.aionemu.chatserver.network.aion.clientpackets;

import java.util.Arrays;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.main.CSConfig;
import com.aionemu.chatserver.configs.main.LoggingConfig;
import com.aionemu.chatserver.dao.ChatLogDAO;
import com.aionemu.chatserver.model.channel.ChatChannels;
import com.aionemu.chatserver.model.channel.RaceChannel;
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
	private BroadcastService broadcastService;

	/**
	 * @param channelBuffer
	 * @param gameChannelHandler
	 * @param opCode
	 */
	public CM_CHANNEL_MESSAGE(ChannelBuffer channelBuffer, ClientChannelHandler gameChannelHandler, BroadcastService broadcastService) {
		super(channelBuffer, gameChannelHandler, 0x18);
		this.broadcastService = broadcastService;
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
		RaceChannel channel = ChatChannels.getChannelById(channelId);
		Message message = new Message(channel, content, clientChannelHandler.getChatClient());
		if (clientChannelHandler.getChatClient().isGagged()) {
			long endTime = (clientChannelHandler.getChatClient().getGagTime() - System.currentTimeMillis()) / 1000 / 60;
			message.setText("You have been gagged for " + endTime + " minutes.");
			clientChannelHandler.sendPacket(new SM_CHANNEL_MESSAGE(message));
			return;
		}
		if (!clientChannelHandler.getChatClient().verifyLastMessage()) {
			message.setText("You can use chat only once every " + CSConfig.MESSAGE_DELAY + " seconds.");
			clientChannelHandler.sendPacket(new SM_CHANNEL_MESSAGE(message));
			return;
		}
		broadcastService.broadcastMessage(message);

		if (LoggingConfig.LOG_CHAT) {
			LoggerFactory.getLogger("CHAT_LOG").info("[{}] {}: {}", message.getChannelString(), message.getSenderName(), message.getTextString());
		}

		if (LoggingConfig.LOG_CHAT_TO_DB) {
			DAOManager.getDAO(ChatLogDAO.class).add_ChannelChat(message.getSenderName(), message.getTextString(), "", message.getChannelString());
		}
	}

	@Override
	public String toString() {
		return "CM_CHANNEL_MESSAGE [channelId=" + channelId + ", content=" + Arrays.toString(content) + "]";
	}
}
