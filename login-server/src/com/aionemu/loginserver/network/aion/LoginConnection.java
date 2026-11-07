package com.aionemu.loginserver.network.aion;

import java.security.interfaces.RSAPrivateKey;
import java.util.concurrent.Executor;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.netty.NettyConnection;
import com.aionemu.commons.network.netty.NettyPipeline;
import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.controller.AccountTimeController;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.network.aion.serverpackets.SM_INIT;
import com.aionemu.loginserver.network.factories.AionPacketHandlerFactory;
import com.aionemu.loginserver.network.ncrypt.CryptEngine;
import com.aionemu.loginserver.network.ncrypt.EncryptedRSAKeyPair;
import com.aionemu.loginserver.network.ncrypt.KeyGen;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * Object representing connection between LoginServer and Aion Client.
 * 
 * @author -Nemesiss-
 */
public class LoginConnection extends NettyConnection<AionServerPacket> {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(LoginConnection.class);

	private static final int MAX_PACKET_SIZE = 8192 * 2;

	/**
	 * Unique Session Id of this connection
	 */
	private int sessionId = hashCode();

	/**
	 * Account object for this connection. if state = AUTHED_LOGIN account cant be null.
	 */
	private Account account;

	/**
	 * Crypt to encrypt/decrypt packets. Only used by the I/O thread of the channel.
	 */
	private final CryptEngine cryptEngine = new CryptEngine();

	/**
	 * True if this user is connecting to GS.
	 */
	private boolean joinedGs;

	/**
	 * Scrambled key pair for RSA
	 */
	private EncryptedRSAKeyPair encryptedRSAKeyPair;

	/**
	 * Session Key for this connection.
	 */
	private SessionKey sessionKey;

	/**
	 * Current state of this connection
	 */
	private volatile State state = State.CONNECTED;

	/**
	 * Possible states of AionConnection
	 */
	public enum State {
		/**
		 * Means that client just connects
		 */
		CONNECTED,

		/**
		 * Means that clients GameGuard is authenticated
		 */
		AUTHED_GG,

		/**
		 * Means that client is logged in.
		 */
		AUTHED_LOGIN
	}

	public LoginConnection(Channel channel, Executor packetExecutor) {
		super(channel, packetExecutor);
	}

	/**
	 * Sets up the channel of an Aion game client.
	 */
	public static void initChannel(Channel channel, Executor packetExecutor) {
		LoginConnection connection = new LoginConnection(channel, packetExecutor);
		NettyPipeline.init(channel, connection, MAX_PACKET_SIZE, MAX_PACKET_SIZE, new LoginCryptHandler(connection, connection.cryptEngine));
	}

	/**
	 * Creates the packet from the decrypted data, which the crypt handler has already verified.
	 */
	@Override
	protected BaseClientPacket<?> createPacket(ByteBuf data) {
		return AionPacketHandlerFactory.handle(data, this);
	}

	@Override
	protected void writePacket(AionServerPacket packet, ByteBuf buffer) {
		packet.write(this, buffer);
	}

	@Override
	protected final void onDisconnect() {
		// Remove account only if not joined GameServer yet.
		if (account != null && !joinedGs) {
			AccountController.removeAccountOnLS(account);
			AccountTimeController.updateOnLogout(account);
		}
	}

	@Override
	protected boolean isAuthenticated() {
		return state == State.AUTHED_LOGIN;
	}

	@Override
	protected long getAuthTimeoutMillis() {
		return Config.CLIENT_AUTH_TIMEOUT_SECONDS * 1000L;
	}

	@Override
	protected int getMaxPendingPackets() {
		return 50;
	}

	@Override
	protected long getMaxPendingWriteBytes() {
		return 256 * 1024;
	}

	@Override
	protected long getMaxSendStallMillis() {
		return 60_000;
	}

	/**
	 * Return Scrambled modulus
	 * 
	 * @return Scrambled modulus
	 */
	public final byte[] getEncryptedModulus() {
		return encryptedRSAKeyPair.getEncryptedModulus();
	}

	/**
	 * Return RSA private key
	 * 
	 * @return rsa private key
	 */
	public final RSAPrivateKey getRSAPrivateKey() {
		return (RSAPrivateKey) encryptedRSAKeyPair.getRSAKeyPair().getPrivate();
	}

	/**
	 * Returns unique sessionId of this connection.
	 * 
	 * @return SessionId
	 */
	public final int getSessionId() {
		return sessionId;
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
	 * Set current state of this connection
	 * 
	 * @param state
	 */
	public final void setState(State state) {
		this.state = state;
	}

	/**
	 * Returns Account object that this client logged in or null
	 * 
	 * @return Account
	 */
	public final Account getAccount() {
		return account;
	}

	/**
	 * Set Account object for this connection.
	 * 
	 * @param account
	 */
	public final void setAccount(Account account) {
		this.account = account;
	}

	/**
	 * Returns Session Key of this connection
	 * 
	 * @return SessionKey
	 */
	public final SessionKey getSessionKey() {
		return sessionKey;
	}

	/**
	 * Set Session Key for this connection
	 * 
	 * @param sessionKey
	 */
	public final void setSessionKey(SessionKey sessionKey) {
		this.sessionKey = sessionKey;
	}

	public boolean isJoinedGs() {
		return joinedGs;
	}

	/**
	 * Set joinedGs value to true
	 */
	public final void setJoinedGs() {
		joinedGs = true;
	}

	/**
	 * @return String info about this connection
	 */
	@Override
	public String toString() {
		return (account == null ? "Client " : account + " ") + getIP();
	}

	@Override
	protected void onConnect() {
		log.info("Connection attempt from: {}", getIP());
		encryptedRSAKeyPair = KeyGen.getEncryptedRSAKeyPair();
		SecretKey blowfishKey = KeyGen.generateBlowfishKey();

		cryptEngine.updateKey(blowfishKey.getEncoded());

		sendPacket(new SM_INIT(this, blowfishKey));
	}
}
