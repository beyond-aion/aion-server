package com.aionemu.gameserver.network;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.netty.ConnectionLimiter;
import com.aionemu.commons.network.netty.NettyServer;
import com.aionemu.commons.utils.concurrent.SequentialExecutor;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.utils.PacketSendUtility;

public class NetConnector {

	private static final Logger log = LoggerFactory.getLogger(NetConnector.class);
	private static NettyServer nettyServer;
	/**
	 * Client packets run on virtual threads, so a packet that waits (e.g. for the database) doesn't hold up the packets of other clients. Each
	 * connection executes its packets sequentially.
	 */
	private static ExecutorService packetExecutor;

	public static void connect() {
		if (NetworkConfig.PACKET_PROCESSOR_STUCK_WARN_SECONDS > 0)
			SequentialExecutor.startWatchdog(Duration.ofSeconds(NetworkConfig.PACKET_PROCESSOR_STUCK_WARN_SECONDS));
		nettyServer = new NettyServer(NetworkConfig.TRANSPORT, NetworkConfig.IO_THREADS);
		packetExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("PacketProcessor-", 0).factory());
		ConnectionLimiter limiter = new ConnectionLimiter(NetworkConfig.MAX_CONNECTIONS, NetworkConfig.MAX_CONNECTIONS_PER_IP,
			NetworkConfig.CONNECTIONS_PER_SECOND_PER_IP, NetworkConfig.CONNECTION_BURST_PER_IP);
		nettyServer.bind(NetworkConfig.CLIENT_SOCKET_ADDRESS, "Aion game clients",
			limiter.wrap(ch -> AionConnection.initChannel(ch, packetExecutor)));
		LoginServer.getInstance().connect(nettyServer);
		if (GSConfig.ENABLE_CHAT_SERVER)
			ChatServer.getInstance().connect(nettyServer);
	}

	public static void shutdown() {
		if (nettyServer == null)
			return;
		if (forceStaffClientsToServerSelectionScreen())
			LockSupport.parkNanos(Duration.ofMillis(200).toNanos()); // wait for login server to accept reconnections
		long clients = findClientConnections().count();
		log.info("Shutting down the network{}", clients == 0 ? "" : ", closing " + clients + " client connection(s)");
		nettyServer.shutdown();
		packetExecutor.shutdown();
		try {
			if (!packetExecutor.awaitTermination(10, TimeUnit.SECONDS))
				log.warn("Client packets were still being executed after 10 seconds");
		} catch (InterruptedException _) {
		}
	}

	public static Stream<AionConnection> findClientConnections() {
		return nettyServer.findConnections(AionConnection.class);
	}

	private static boolean forceStaffClientsToServerSelectionScreen() {
		List<AionConnection> staffClients = findClientConnections().filter(c -> c.getAccount() != null && c.getAccount().getAccessLevel() > 0).toList();
		if (staffClients.isEmpty())
			return false;
		staffClients.forEach(c -> {
			if (c.getState() == AionConnection.State.IN_GAME)
				PacketSendUtility.sendPacket(c.getActivePlayer(), SM_QUIT_RESPONSE.quitToEditScreen());
			LoginServer.getInstance().requestAuthReconnection(c.getAccount().getId(), c);
		});
		return true;
	}
}
