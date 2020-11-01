package com.aionemu.chatserver.network.netty.pipeline;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import com.aionemu.chatserver.network.aion.ClientPacketHandler;
import com.aionemu.chatserver.network.netty.coder.LoginPacketDecoder;
import com.aionemu.chatserver.network.netty.coder.LoginPacketEncoder;
import com.aionemu.chatserver.network.netty.coder.PacketFrameDecoder;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

/**
 * @author ATracer
 */
public class LoginToClientPipeLineFactory implements ChannelPipelineFactory {

	private static final int THREADS_MAX = 10;
	private static final int MEMORY_PER_CHANNEL = 1048576;
	private static final int TOTAL_MEMORY = 134217728;
	private static final int TIMEOUT = 100;
	private final ClientPacketHandler clientPacketHandler;
	private final ExecutionHandler executionHandler;

	public LoginToClientPipeLineFactory(ClientPacketHandler clientPacketHandler) {
		this.clientPacketHandler = clientPacketHandler;
		this.executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(THREADS_MAX, MEMORY_PER_CHANNEL, TOTAL_MEMORY, TIMEOUT,
			TimeUnit.MILLISECONDS, Executors.defaultThreadFactory()));
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("framedecoder", new PacketFrameDecoder());
		pipeline.addLast("packetdecoder", new LoginPacketDecoder());
		pipeline.addLast("packetencoder", new LoginPacketEncoder());
		pipeline.addLast("executor", executionHandler);
		pipeline.addLast("handler", new ClientChannelHandler(clientPacketHandler));
		return pipeline;
	}
}
