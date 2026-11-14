package com.aionemu.gameserver.network.aion;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.netty.NettyPipeline;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.sequrity.FloodManager;
import com.aionemu.gameserver.network.sequrity.FloodManager.Result;

import io.netty.channel.Channel;
import io.netty.handler.flush.FlushConsolidationHandler;

/**
 * Sets up the channels of Aion game clients, or closes them if their IP opens too many connections.
 *
 * @author -Nemesiss-
 */
public class AionChannelInitializer implements Consumer<Channel> {

	/**
	 * Larger client packets close the connection.
	 */
	static final int MAX_RECEIVED_PACKET_SIZE = 8192 * 4;

	private final Logger log = LoggerFactory.getLogger(AionChannelInitializer.class);
	private FloodManager floodAcceptor;

	public AionChannelInitializer() {
		if (NetworkConfig.ENABLE_FLOOD_CONNECTIONS) {
			floodAcceptor = new FloodManager(NetworkConfig.Flood_Tick, new FloodManager.FloodFilter(NetworkConfig.Flood_SWARN, NetworkConfig.Flood_SReject,
				NetworkConfig.Flood_STick), // short period
				new FloodManager.FloodFilter(NetworkConfig.Flood_LWARN, NetworkConfig.Flood_LReject, NetworkConfig.Flood_LTick)); // long period
		}
	}

	@Override
	public void accept(Channel channel) {
		if (floodAcceptor != null) {
			String host = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
			Result isFlooding = floodAcceptor.isFlooding(host, true);
			switch (isFlooding) {
				case REJECTED:
					log.warn("Rejected connection from {}", host);
					channel.close();
					return;
				case WARNED:
					log.warn("Connection over warn limit from {}", host);
					break;
			}
		}
		init(channel, new AionConnection(channel));
	}

	static void init(Channel channel, AionConnection connection) {
		NettyPipeline.init(channel, connection, MAX_RECEIVED_PACKET_SIZE, AionServerPacket.MAX_CLIENT_SUPPORTED_PACKET_SIZE,
			new AionCryptHandler(connection, connection.getCrypt()));
		// broadcasts write many packets in a row, this flushes them together
		channel.pipeline().addFirst("flushConsolidation", new FlushConsolidationHandler(FlushConsolidationHandler.DEFAULT_EXPLICIT_FLUSH_AFTER_FLUSHES, true));
	}
}
