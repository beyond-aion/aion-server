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

/**
 * @author ATracer
 */
public class CM_CHANNEL_MESSAGE extends AbstractClientPacket {

	private int channelId;
	private byte[] content;

	public CM_CHANNEL_MESSAGE(ChannelBuffer channelBuffer, ClientChannelHandler clientChannelHandler, byte opCode) {
		super(channelBuffer, clientChannelHandler, opCode);
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
		int contentLength = readH() * 2;
		content = readB(contentLength);
	}

	@Override
	protected void runImpl() {
		Channel channel = ChatChannels.getChannelById(channelId);
		if (channel == null)
			return;
		ChatClient client = clientChannelHandler.getChatClient();
		Message message = new Message(channel, content, client);
		if (client.isGagged()) {
			long gagTimeMin = (client.getGagTime() - System.currentTimeMillis()) / 1000 / 60;
			message.setText("You have been gagged for " + gagTimeMin + " minutes.");
			clientChannelHandler.sendPacket(new SM_CHANNEL_MESSAGE(message));
			return;
		}
		int floodProtectionTime = client.nextMessageTimeSec(channel.getChannelType());
		if (floodProtectionTime > 0) {
			message.setText("You can chat again in this channel in " + floodProtectionTime + " second" + (floodProtectionTime == 1 ? "." : "s."));
			clientChannelHandler.sendPacket(new SM_CHANNEL_MESSAGE(message));
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
