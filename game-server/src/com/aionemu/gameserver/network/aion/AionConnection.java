package com.aionemu.gameserver.network.aion;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.network.netty.NettyConnection;
import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.commons.network.packet.BasePacket;
import com.aionemu.commons.utils.concurrent.ExecuteWrapper;
import com.aionemu.commons.utils.concurrent.RunnableStatsManager;
import com.aionemu.commons.utils.concurrent.SequentialExecutor;
import com.aionemu.gameserver.configs.main.ThreadConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.configs.network.PffConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.Crypt;
import com.aionemu.gameserver.network.aion.clientpackets.CM_PING;
import com.aionemu.gameserver.network.aion.serverpackets.SM_KEY;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.services.player.PlayerLeaveWorldService;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * Object representing connection between GameServer and Aion Client.
 *
 * @author -Nemesiss-
 */
public class AionConnection extends NettyConnection<AionServerPacket> {

	private static final Logger log = LoggerFactory.getLogger(AionConnection.class);

	/**
	 * Client packets run on virtual threads, so a packet that waits (e.g. for the database) doesn't hold up the packets of other clients. Each
	 * connection executes its packets one after another.
	 */
	private static final ExecutorService packetThreads = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("PacketProcessor-", 0).factory());

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
	 * Current state of this connection
	 */
	private volatile State state;

	/**
	 * AionClient is authenticating by passing to GameServer id of account.
	 */
	private final AtomicReference<Account> account = new AtomicReference<>();

	/**
	 * Crypt that will encrypt/decrypt packets. Only used by the I/O thread of the channel.
	 */
	private final Crypt crypt = new Crypt();

	/**
	 * active Player that owner of this connection is playing [entered game]
	 */
	private final AtomicReference<Player> activePlayer = new AtomicReference<>();

	private volatile long lastClientMessageTime;
	private volatile long lastPingTime;
	private volatile int pingFailCount;

	private String macAddress;
	private String hddSerial;

	private ScheduledFuture<?> connectionAliveChecker;

	/** packet flood filter **/
	private Map<Integer, Long> pffRequests;

	public AionConnection(Channel channel) {
		this(channel, new SequentialExecutor(packetThreads));
	}

	AionConnection(Channel channel, Executor packetExecutor) {
		super(channel, packetExecutor);

		state = State.CONNECTED;
		lastClientMessageTime = System.currentTimeMillis();

		if (PffConfig.PFF_MODE > 0 && PffConfig.THRESHOLD_MILLIS_BY_PACKET_OPCODE != null)
			pffRequests = new ConcurrentHashMap<>();
	}

	Crypt getCrypt() {
		return crypt;
	}

	@Override
	protected void onConnect() {
		log.debug("connection from: {}", getIP());
		connectionAliveChecker = getChannel().eventLoop().scheduleAtFixedRate(this::closeIfHangedUp, CM_PING.CLIENT_PING_INTERVAL,
			CM_PING.CLIENT_PING_INTERVAL, TimeUnit.MILLISECONDS);
		sendPacket(new SM_KEY());
	}

	private void closeIfHangedUp() {
		long millisSinceLastClientPacket = System.currentTimeMillis() - lastClientMessageTime;
		if (millisSinceLastClientPacket - 5000 > CM_PING.CLIENT_PING_INTERVAL) {
			log.info("Closing hanged up connection of {} (last sign of life was {} ms ago)", this, millisSinceLastClientPacket);
			close();
		}
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
	 * Creates the packet from decrypted data and applies the packet flood filter.
	 */
	@Override
	protected final BaseClientPacket<?> createPacket(ByteBuf data) {
		AionClientPacket pck = AionClientPacketFactory.tryCreatePacket(data, this);
		if (pck == null)
			return null;

		lastClientMessageTime = System.currentTimeMillis();
		if (pffRequests != null) {
			int msBetweenPackets = PffConfig.getAllowedMillisBetweenPackets(pck);
			if (msBetweenPackets > 0) {
				Long last = pffRequests.put(pck.getOpCode(), lastClientMessageTime);
				if (last != null) {
					long diff = lastClientMessageTime - last;
					if (diff < msBetweenPackets) {
						log.warn("{} is flooding {} (last diff: {} ms)", this, pck.getClass().getSimpleName(), diff);
						if (PffConfig.PFF_MODE == 2) { // disconnect
							getChannel().close();
							return null;
						}
					}
				}
			}
		}
		return pck;
	}

	/**
	 * Some clients send packets shorter than the server expects, they are just ignored.
	 */
	@Override
	protected boolean isReadFailureFatal() {
		return false;
	}

	@Override
	protected void runPacket(BaseClientPacket<?> packet) {
		sendPacketInfo(packet);
		ExecuteWrapper.execute(packet, ThreadConfig.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING, true);
	}

	@Override
	protected final void writePacket(AionServerPacket packet, ByteBuf buffer) {
		if (packet.getClass() != SM_MESSAGE.class && canReceivePacketInfoInChat()) // sent after this packet, it's being written right now
			getChannel().eventLoop().execute(() -> sendPacketInfo(packet));
		long begin = System.nanoTime();
		packet.write(this, buffer);
		if (CommonsConfig.RUNNABLESTATS_ENABLE) {
			long duration = System.nanoTime() - begin;
			RunnableStatsManager.handleStats(packet.getClass(), "runImpl()", duration);
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
		if (connectionAliveChecker != null)
			connectionAliveChecker.cancel(false);
		LoginServer.getInstance().onDisconnect(this);

		String msg = getAccount() == null ? "" : " " + getAccount();
		Player player = getActivePlayer();
		if (player != null && !PlayerLeaveWorldService.isLeavingWorld(player)) {
			msg += " " + player + " (client crash or connection loss)";
			player.getMoveController().resetToLastPositionFromClient(); // avoid mapkick and bugging through walls
			PlayerLeaveWorldService.registerLeaveWorld(player);
		}

		if (msg.isEmpty())
			msg = " " + this;

		log.info("Client disconnected:{}", msg);
	}

	@Override
	protected boolean isAuthenticated() {
		return state != State.CONNECTED;
	}

	@Override
	protected long getAuthTimeoutMillis() {
		return NetworkConfig.CLIENT_AUTH_TIMEOUT_SECONDS * 1000L;
	}

	@Override
	protected int getMaxPendingPackets() {
		return NetworkConfig.PACKET_PROCESSOR_MAX_PENDING_PACKETS_PER_CONNECTION;
	}

	@Override
	protected long getMaxPendingWriteBytes() {
		return NetworkConfig.MAX_PENDING_SEND_KILOBYTES * 1024L;
	}

	@Override
	protected long getMaxSendStallMillis() {
		return NetworkConfig.MAX_SEND_STALL_SECONDS * 1000L;
	}

	/**
	 * Waits for the client packets and disconnect handlers that are still queued or running. Call after the network threads were stopped.
	 *
	 * @return True if all of them finished within the timeout.
	 */
	public static boolean shutdownPacketExecution(Duration timeout) {
		packetThreads.shutdown();
		try {
			return packetThreads.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
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

	public long getLastPingTime() {
		return lastPingTime;
	}

	public void setLastPingTime(long time) {
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
}
