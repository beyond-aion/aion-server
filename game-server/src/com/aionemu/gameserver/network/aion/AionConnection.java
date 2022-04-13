package com.aionemu.gameserver.network.aion;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.network.AConnection;
import com.aionemu.commons.network.Dispatcher;
import com.aionemu.commons.network.PacketProcessor;
import com.aionemu.commons.network.packet.BasePacket;
import com.aionemu.commons.utils.concurrent.ExecuteWrapper;
import com.aionemu.commons.utils.concurrent.RunnableStatsManager;
import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.configs.main.ThreadConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.configs.network.PffConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.Crypt;
import com.aionemu.gameserver.network.aion.clientpackets.CM_PING;
import com.aionemu.gameserver.network.aion.clientpackets.CM_PING_INGAME;
import com.aionemu.gameserver.network.aion.serverpackets.SM_KEY;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.services.player.PlayerLeaveWorldService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Object representing connection between GameServer and Aion Client.
 * 
 * @author -Nemesiss-
 */
public class AionConnection extends AConnection<AionServerPacket> {

	private static final Logger log = LoggerFactory.getLogger(AionConnection.class);

	private static final PacketProcessor<AionConnection> packetProcessor = new PacketProcessor<>(NetworkConfig.PACKET_PROCESSOR_MIN_THREADS,
		NetworkConfig.PACKET_PROCESSOR_MAX_THREADS, NetworkConfig.PACKET_PROCESSOR_THREAD_SPAWN_THRESHOLD,
		NetworkConfig.PACKET_PROCESSOR_THREAD_KILL_THRESHOLD, new ExecuteWrapper(ThreadConfig.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING));

	/**
	 * Possible states of AionConnection
	 */
	public enum State {
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
	private final Deque<AionServerPacket> sendMsgQueue = new ArrayDeque<>();

	/**
	 * Current state of this connection
	 */
	private volatile State state;

	/**
	 * AionClient is authenticating by passing to GameServer id of account.
	 */
	private final AtomicReference<Account> account = new AtomicReference<>();

	/**
	 * Crypt that will encrypt/decrypt packets.
	 */
	private final Crypt crypt = new Crypt();

	/**
	 * active Player that owner of this connection is playing [entered game]
	 */
	private final AtomicReference<Player> activePlayer = new AtomicReference<>();

	private volatile long lastClientMessageTime;
	private volatile long lastPingTime, lastPingPongPingTime;
	private volatile int pingFailCount;

	private final static int MAX_CORRUPT_PACKETS_BEFORE_DISCONNECT = 3;
	private volatile int corruptPackets = 0;

	private String macAddress;
	private String hddSerial;

	private ConnectionAliveChecker connectionAliveChecker;

	/** packet flood filter **/
	private Map<Integer, Long> pffRequests;

	public AionConnection(SocketChannel sc, Dispatcher d) throws IOException {
		super(sc, d, 8192 * 4, 8192 * 4);

		state = State.CONNECTED;

		String ip = getIP();
		log.debug("connection from: " + ip);

		lastClientMessageTime = System.currentTimeMillis();
		connectionAliveChecker = new ConnectionAliveChecker();

		if (PffConfig.PFF_MODE > 0 && PffConfig.THRESHOLD_MILLIS_BY_PACKET_OPCODE != null)
			pffRequests = new ConcurrentHashMap<>();
	}

	@Override
	protected void initialized() {
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

	@Override
	protected final Queue<AionServerPacket> getSendMsgQueue() {
		return sendMsgQueue;
	}

	/**
	 * Called by Dispatcher. ByteBuffer data contains one packet that should be processed.
	 * 
	 * @param data
	 * @return True if data was processed correctly, False if some error occurred and connection should be closed NOW.
	 */
	@Override
	protected final boolean processData(ByteBuffer data) {
		if (!crypt.isEnabled()) // skip unprocessable packet (client sends crap upon reconnect, just before Crypt gets initialized and sent via SM_KEY)
			return true;

		if (!crypt.decrypt(data)) {
			if (++corruptPackets >= MAX_CORRUPT_PACKETS_BEFORE_DISCONNECT) {
				log.warn("Client packet decryption failed " + corruptPackets + " times, disconnecting " + this);
				return false;
			}
			log.debug("[" + corruptPackets + "/" + MAX_CORRUPT_PACKETS_BEFORE_DISCONNECT + "] Decrypt fail, client packet passed...");
			return true;
		}

		if (data.remaining() < 5) {// op + static code + op == 5 bytes
			log.warn("Received fake packet from " + this + ", disconnecting");
			return false;
		}

		AionClientPacket pck = AionClientPacketFactory.tryCreatePacket(data, this);

		// Execute packet only if packet exist (!= null) and read was ok.
		if (pck != null) {
			lastClientMessageTime = System.currentTimeMillis();
			if (pffRequests != null) {
				int msBetweenPackets = PffConfig.getAllowedMillisBetweenPackets(pck);
				if (msBetweenPackets > 0) {
					Long last = pffRequests.put(pck.getOpCode(), lastClientMessageTime);
					if (last != null) {
						long diff = lastClientMessageTime - last;
						if (diff < msBetweenPackets) {
							log.warn(this + " is flooding " + pck.getClass().getSimpleName() + " (last diff: " + diff + "ms)");
							if (PffConfig.PFF_MODE == 1) // disconnect
								return false;
						}
					}
				}
			}

			if (pck.read()) {
				sendPacketInfo(pck);
				packetProcessor.executePacket(pck);
			}
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
			if (sendMsgQueue.isEmpty())
				return false;
			AionServerPacket packet = sendMsgQueue.removeFirst();
			if (packet.getClass() != SM_MESSAGE.class)
				sendPacketInfo(packet);
			long begin = System.nanoTime();
			packet.write(this, data);
			if (CommonsConfig.RUNNABLESTATS_ENABLE) {
				long duration = System.nanoTime() - begin;
				RunnableStatsManager.handleStats(packet.getClass(), "runImpl()", duration);
			}
			return true;
		}
	}

	private boolean canReceivePacketInfoInChat() {
		return getState() == State.IN_GAME && getAccount().getMembership() == 10;
	}

	private void sendPacketInfo(BasePacket packet) {
		if (canReceivePacketInfoInChat())
			sendPacket(new SM_MESSAGE(0, null, packet.toFormattedPacketNameString(), ChatType.BRIGHT_YELLOW));
	}

	void sendUnknownClientPacketInfo(int opCode) {
		if (canReceivePacketInfoInChat())
			sendPacket(new SM_MESSAGE(0, null, BasePacket.toFormattedPacketNameString(3, opCode, "CM_UNK"), ChatType.YELLOW));
	}

	@Override
	protected final void onDisconnect() {
		connectionAliveChecker.stop();
		if (GameServer.isShuttingDownSoon()) { // client crashing during last seconds of countdown
			safeLogout(); // instant synchronized leaveWorld to ensure completion before onServerClose
			return;
		}

		String msg = "";
		Account account = getAccount();
		if (account != null) {
			msg += " " + account;
			LoginServer.getInstance().aionClientDisconnected(account.getId());
		}

		Player player = getActivePlayer();
		if (player != null) {
			msg += " " + player + " (client crash or connection loss)";
			player.getMoveController().resetToLastPositionFromClient(); // avoid mapkick and bugging through walls
			long millisSinceLastClientPacket = System.currentTimeMillis() - lastClientMessageTime;
			long delayMs = Math.max(0, TimeUnit.SECONDS.toMillis(10) - millisSinceLastClientPacket);
			PlayerLeaveWorldService.leaveWorldDelayed(player, delayMs); // delayed to prevent ctrl+alt+del / close window exploit
		}

		if (msg.isEmpty())
			msg = " " + this;

		log.info("Client disconnected:" + msg);
	}

	@Override
	protected final void onServerClose() {
		close();
		safeLogout();
	}

	private void safeLogout() {
		synchronized (this) {
			Player player = getActivePlayer();
			if (player == null) // player was already saved
				return;
			try {
				PlayerLeaveWorldService.leaveWorld(player);
			} catch (Exception e) {
				log.error("Error saving " + player, e);
			}
		}
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
		return account.get();
	}

	/**
	 * Sets account object associated with this connection
	 * 
	 * @param account
	 *          account object associated with this connection
	 */
	public void setAccount(Account account) {
		this.account.set(Objects.requireNonNull(account, "Account can't be null"));
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

	public long getLastClientMessageTime() {
		return lastClientMessageTime;
	}

	/**
	 * @param pingPong
	 *          if true returns CM_PING time (less frequent than CM_PING_INGAME)
	 * @return Last ping time from client. Either from CM_PING or from CM_PING_INGAME.
	 */
	public long getLastPingTime(boolean pingPong) {
		if (pingPong)
			return lastPingPongPingTime;
		return lastPingTime;
	}

	public void setLastPingTime(long time, boolean pingPong) {
		if (pingPong)
			lastPingPongPingTime = time;
		else
			lastPingTime = time;
	}

	public int increaseAndGetPingFailCount() {
		return ++pingFailCount;
	}

	public void resetPingFailCount() {
		pingFailCount = 0;
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
		return "AionConnection [state=" + state + ", account=" + getAccount() + ", activePlayer=" + activePlayer.get() + ", macAddress=" + macAddress
			+ ", getIP()=" + getIP() + "]";
	}

	private class ConnectionAliveChecker implements Runnable {

		private ScheduledFuture<?> task;

		private ConnectionAliveChecker() {
			if (connectionAliveChecker != null)
				throw new IllegalStateException("ConnectionAliveChecker for " + AionConnection.this + " is already assigned.");
			int checkIntervalMillis = Math.min(CM_PING_INGAME.CLIENT_PING_INTERVAL, CM_PING.CLIENT_PING_INTERVAL);
			task = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, checkIntervalMillis * 2, checkIntervalMillis);
		}

		private void stop() {
			task.cancel(false);
		}

		@Override
		public void run() {
			Player player = getActivePlayer();
			// just checking lastPingTime is not sufficient, CM_PING_INGAME interval seems to vary or skip from time to time / under certain circumstances
			long millisSinceLastClientPacket = System.currentTimeMillis() - lastClientMessageTime;
			if (millisSinceLastClientPacket - 5000 > getMaxInactivityTimeMillis(player)) {
				log.info("Closing hanged up connection of " + AionConnection.this + " (last sign of life was " + millisSinceLastClientPacket + "ms ago)");
				close();
			}
		}

		private long getMaxInactivityTimeMillis(Player player) {
			if (player == null || !player.isSpawned())
				return CM_PING.CLIENT_PING_INTERVAL;
			if (player.getController().isInCombat())
				return CM_PING_INGAME.CLIENT_PING_INTERVAL;
			if (player.isInCustomState(CustomPlayerState.WATCHING_CUTSCENE))
				return TimeUnit.MINUTES.toMillis(4);
			return CM_PING_INGAME.CLIENT_PING_INTERVAL * 2;
		}
	}
}
