package com.aionemu.chatserver.network.netty.handler;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.AbstractServerPacket;
import com.aionemu.chatserver.network.aion.ClientPacketHandler;

/**
 * @author ATracer
 */
public class ClientChannelHandler extends AbstractChannelHandler {

	private static final Logger log = LoggerFactory.getLogger(ClientChannelHandler.class);
	private final ClientPacketHandler clientPacketHandler;
	private ClientChannelHandlerState state;
	private ChatClient chatClient;

	public ClientChannelHandler(ClientPacketHandler clientPacketHandler) {
		this.clientPacketHandler = clientPacketHandler;
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelConnected(ctx, e);
		state = ClientChannelHandlerState.CONNECTED;
		inetAddress = ((InetSocketAddress) e.getChannel().getRemoteAddress()).getAddress();
		associatedChannel = ctx.getChannel();
		log.info("Channel connected Ip: {}", inetAddress.getHostAddress());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		super.messageReceived(ctx, e);
		AbstractClientPacket clientPacket = clientPacketHandler.handle((ChannelBuffer) e.getMessage(), this);
		if (clientPacket != null && clientPacket.read())
			clientPacket.run();
		if (clientPacket != null)
			log.debug("Received packet: {}", clientPacket);
	}

	public void sendPacket(AbstractServerPacket packet) {
		ChannelBuffer cb = ChannelBuffers.buffer(ByteOrder.LITTLE_ENDIAN, 2 * 8192);
		packet.write(this, cb);
		associatedChannel.write(cb);
		log.debug("Sent packet: {}", packet);
	}

	public ClientChannelHandlerState getState() {
		return state;
	}

	public void setState(ClientChannelHandlerState state) {
		this.state = state;
	}

	public ChatClient getChatClient() {
		return chatClient;
	}

	public void setChatClient(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	public enum ClientChannelHandlerState {

		CONNECTED,
		AUTHED

	}
}
