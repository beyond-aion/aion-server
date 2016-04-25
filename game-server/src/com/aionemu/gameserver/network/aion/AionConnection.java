package com.aionemu.gameserver.network.aion;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.AConnection;
import com.aionemu.commons.network.Dispatcher;
import com.aionemu.commons.network.PacketProcessor;
import com.aionemu.commons.utils.concurrent.ExecuteWrapper;
import com.aionemu.commons.utils.concurrent.RunnableStatsManager;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.Crypt;
import com.aionemu.gameserver.network.PacketFloodFilter;
import com.aionemu.gameserver.network.aion.clientpackets.CM_PING;
import com.aionemu.gameserver.network.aion.serverpackets.SM_KEY;
import com.aionemu.gameserver.network.factories.AionPacketHandlerFactory;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.services.player.PlayerLeaveWorldService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.google.common.base.Preconditions;

import javolution.util.FastTable;

/**
 * Object representing connection between GameServer and Aion Client.
 * 
 * @author -Nemesiss-
 */
public class AionConnection extends AConnection {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(AionConnection.class);

	private static final PacketProcessor<AionConnection> packetProcessor = new PacketProcessor<AionConnection>(
		NetworkConfig.PACKET_PROCESSOR_MIN_THREADS, NetworkConfig.PACKET_PROCESSOR_MAX_THREADS, NetworkConfig.PACKET_PROCESSOR_THREAD_SPAWN_THRESHOLD,
		NetworkConfig.PACKET_PROCESSOR_THREAD_KILL_THRESHOLD, new ExecuteWrapper());

	/**
	 * Possible states of AionConnection
	 */
	public static enum State {
		/**
		 * client just connect
		 */
		CONNECTED,
		/**
		 * client is authenticated
		 */
		AUTHED,
		/**
		 * client entered world.
		 */
		IN_GAME
	}

	/**
	 * Server Packet "to send" Queue
	 */
	private final FastTable<AionServerPacket> sendMsgQueue = new FastTable<AionServerPacket>();

	/**
	 * Current state of this connection
	 */
	private volatile State state;

	/**
	 * AionClient is authenticating by passing to GameServer id of account.
	 */
	private Account account;

	/**
	 * Crypt that will encrypt/decrypt packets.
	 */
	private final Crypt crypt = new Crypt();

	/**
	 * active Player that owner of this connection is playing [entered game]
	 */
	private AtomicReference<Player> activePlayer = new AtomicReference<Player>();

	private AionPacketHandler aionPacketHandler;
	private long lastPingTime;

	private int nbInvalidPackets = 0;
	// TODO! why there is no any comments what is this doing? i have no clue what is it for [Nemesiss]
	private final static int MAX_INVALID_PACKETS = 3;

	private String macAddress;
	private String hddSerial;

	/** Ping checker - for detecting hanged up connections **/
	private PingChecker pingChecker;

	/** packet flood filter **/
	private int[] pff;
	private long[] pffRequests;

	/**
	 * Constructor
	 * 
	 * @param sc
	 * @param d
	 * @throws IOException
	 */
	public AionConnection(SocketChannel sc, Dispatcher d) throws IOException {
		super(sc, d, 8192 * 4, 8192 * 4);

		AionPacketHandlerFactory aionPacketHandlerFactory = AionPacketHandlerFactory.getInstance();
		this.aionPacketHandler = aionPacketHandlerFactory.getPacketHandler();

		state = State.CONNECTED;

		String ip = getIP();
		log.debug("connection from: " + ip);

		pingChecker = new PingChecker();

		if (SecurityConfig.PFF_ENABLE) {
			pff = PacketFloodFilter.getInstance().getPackets();
			pffRequests = new long[pff.length];
		}
	}

	@Override
	protected void initialized() {
		/** Send SM_KEY packet */
		sendPacket(new SM_KEY());
	}

	/**
	 * Enable crypt key - generate random key that will be used to encrypt second server packet [first one is unencrypted] and decrypt client packets.
	 * This method is called from SM_KEY server packet, that packet sends key to aion client.
	 * 
	 * @return "false key" that should by used by aion client to encrypt/decrypt packets.
	 */
	public final int enableCryptKey() {
		return crypt.enableKey();
	}

	/**
	 * Called by Dispatcher. ByteBuffer data contains one packet that should be processed.
	 * 
	 * @param data
	 * @return True if data was processed correctly, False if some error occurred and connection should be closed NOW.
	 */
	@Override
	protected final boolean processData(ByteBuffer data) {
		try {
			if (!crypt.decrypt(data)) {
				nbInvalidPackets++;
				log.debug("[" + nbInvalidPackets + "/" + MAX_INVALID_PACKETS + "] Decrypt fail, client packet passed...");
				if (nbInvalidPackets >= MAX_INVALID_PACKETS) {
					log.warn("Decrypt fail!");
					return false;
				}
				return true;
			}
		} catch (Exception ex) {
			log.error("Exception caught during decrypt!" + ex.getMessage());
			return false;
		}

		if (data.remaining() < 5) {// op + static code + op == 5 bytes
			log.error("Received fake packet from: " + this);
			return false;
		}

		AionClientPacket pck = aionPacketHandler.handle(data, this);

		/**
		 * Execute packet only if packet exist (!= null) and read was ok.
		 */
		if (pck != null) {
			if (SecurityConfig.PFF_ENABLE) {
				int opcode = pck.getOpcode();
				if (pff.length > opcode) {
					if (pff[opcode] > 0) {
						long last = this.pffRequests[opcode];
						if (last == 0)
							this.pffRequests[opcode] = System.currentTimeMillis();
						else {
							long diff = System.currentTimeMillis() - last;
							if (diff < pff[opcode]) {
								log.warn(this + " has flooding " + pck.getClass().getSimpleName() + " " + diff);
								switch (SecurityConfig.PFF_LEVEL) {
									case 1: // disconnect
										return false;
									case 2:
										break;
								}
							} else
								this.pffRequests[opcode] = System.currentTimeMillis();
						}
					}
				}
			}

			if (pck.read())
				packetProcessor.executePacket(pck);
		}

		return true;
	}

	/**
	 * This method will be called by Dispatcher, and will be repeated till return false.
	 * 
	 * @param data
	 * @return True if data was written to buffer, False indicating that there are not any more data to write.
	 */
	@Override
	protected final boolean writeData(ByteBuffer data) {
		synchronized (guard) {
			final long begin = System.nanoTime();
			if (sendMsgQueue.isEmpty())
				return false;
			AionServerPacket packet = sendMsgQueue.removeFirst();
			try {
				packet.write(this, data);
				return true;
			} finally {
				RunnableStatsManager.handleStats(packet.getClass(), "runImpl()", System.nanoTime() - begin);
			}

		}
	}

	@Override
	protected final void onDisconnect() {
		pingChecker.stop();
		String msg = "";
		if (getAccount() != null) {
			int id = getAccount().getId();
			msg += " [Account ID: " + id + " Name: " + getAccount().getName() + "]";
			LoginServer.getInstance().aionClientDisconnected(id);
		}

		Player player = getActivePlayer();
		if (player != null) {
			msg += " [Player: " + player.getName() + "]";
			// force stop movement of player
			player.getMoveController().abortMove(false);
			player.getController().stopMoving();

			ThreadPoolManager.getInstance().schedule(() -> PlayerLeaveWorldService.leaveWorld(player), 10 * 1000); // prevent ctrl+alt+del / close window exploit
		}

		if (!msg.isEmpty())
			log.info("Client disconnected" + msg);
	}

	@Override
	protected final void onServerClose() {
		// TODO mb some packet should be send to client before closing?
		close(/* packet */);
	}

	/**
	 * Encrypt packet.
	 * 
	 * @param buf
	 */
	public final void encrypt(ByteBuffer buf) {
		crypt.encrypt(buf);
	}

	/**
	 * Sends AionServerPacket to this client.
	 * 
	 * @param bp
	 *          AionServerPacket to be sent.
	 */
	public final void sendPacket(AionServerPacket bp) {
		synchronized (guard) {
			/**
			 * Connection is already closed or waiting for last (close packet) to be sent
			 */
			if (isWriteDisabled())
				return;

			sendMsgQueue.addLast(bp);
			enableWriteInterest();
		}
	}

	/**
	 * Its guaranteed that closePacket will be sent before closing connection, but all past and future packets wont. Connection will be closed [by
	 * Dispatcher Thread], and onDisconnect() method will be called to clear all other things.
	 * 
	 * @param closePacket
	 *          Packet that will be send before closing.
	 */
	public final void close(AionServerPacket closePacket) {
		synchronized (guard) {
			if (isWriteDisabled())
				return;

			pendingClose = true;
			sendMsgQueue.clear();
			sendMsgQueue.addLast(closePacket);
			enableWriteInterest();
		}
	}

	/**
	 * Current state of this connection
	 * 
	 * @return state
	 */
	public final State getState() {
		return state;
	}

	/**
	 * Sets the state of this connection
	 * 
	 * @param state
	 *          state of this connection
	 */
	public void setState(State state) {
		this.state = state;
	}

	/**
	 * Returns account object associated with this connection
	 * 
	 * @return account object associated with this connection
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * Sets account object associated with this connection
	 * 
	 * @param account
	 *          account object associated with this connection
	 */
	public void setAccount(Account account) {
		Preconditions.checkArgument(account != null, "Account can't be null");
		this.account = account;
	}

	/**
	 * Sets Active player to new value. Update connection state to correct value.
	 * 
	 * @param player
	 * @return True if active player was set to new value.
	 */
	public boolean setActivePlayer(Player player) {
		if (player == null) {
			activePlayer.set(player);
			setState(State.AUTHED);
		} else if (activePlayer.compareAndSet(null, player)) {
			setState(State.IN_GAME);
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Return active player or null.
	 * 
	 * @return active player or null.
	 */
	public Player getActivePlayer() {
		return activePlayer.get();
	}

	public long getLastPingTime() {
		return lastPingTime;
	}

	public void setLastPingTime() {
		this.lastPingTime = System.currentTimeMillis();
	}

	public void setMacAddress(String mac) {
		this.macAddress = mac;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public String getHddSerial() {
		return hddSerial;
	}

	public void setHddSerial(String hddSerial) {
		this.hddSerial = hddSerial;
	}

	@Override
	public String toString() {
		return "AionConnection [state=" + state + ", account=" + account + ", activePlayer=" + activePlayer.get() + ", macAddress=" + macAddress + ", getIP()="
			+ getIP() + "]";
	}

	private class PingChecker implements Runnable {

		private ScheduledFuture<?> task;

		private PingChecker() {
			if (AionConnection.this.pingChecker != null)
				throw new IllegalStateException("PingChecker for " + AionConnection.this + " is already assigned.");
			task = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, CM_PING.CLIENT_PING_INTERVAL * 2, CM_PING.CLIENT_PING_INTERVAL);
		}

		private void stop() {
			task.cancel(false);
		}

		@Override
		public void run() {
			if (System.currentTimeMillis() - getLastPingTime() > CM_PING.CLIENT_PING_INTERVAL + 60 * 1000) { // close connection if at least 1 minute overdue
				log.info("Closing hanged up connection of client: " + AionConnection.this);
				close();
				stop();
			}
		}
	}
}
