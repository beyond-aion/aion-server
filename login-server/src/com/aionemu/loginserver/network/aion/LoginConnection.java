package com.aionemu.loginserver.network.aion;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.AConnection;
import com.aionemu.commons.network.Dispatcher;
import com.aionemu.commons.network.PacketProcessor;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.controller.AccountTimeController;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.network.aion.serverpackets.SM_INIT;
import com.aionemu.loginserver.network.factories.AionPacketHandlerFactory;
import com.aionemu.loginserver.network.ncrypt.CryptEngine;
import com.aionemu.loginserver.network.ncrypt.EncryptedRSAKeyPair;
import com.aionemu.loginserver.network.ncrypt.KeyGen;

/**
 * Object representing connection between LoginServer and Aion Client.
 * 
 * @author -Nemesiss-
 */
public class LoginConnection extends AConnection<AionServerPacket> {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(LoginConnection.class);
	/**
	 * PacketProcessor for executing packets.
	 */
	private final static PacketProcessor<LoginConnection> processor = new PacketProcessor<>(1, 8, 50, 3);
	/**
	 * Server Packet "to send" Queue
	 */
	private final Deque<AionServerPacket> sendMsgQueue = new ArrayDeque<>();

	/**
	 * Unique Session Id of this connection
	 */
	private int sessionId = hashCode();

	/**
	 * Account object for this connection. if state = AUTHED_LOGIN account cant be null.
	 */
	private Account account;

	/**
	 * Crypt to encrypt/decrypt packets
	 */
	private CryptEngine cryptEngine;

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
	private State state;

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

	public LoginConnection(SocketChannel sc, Dispatcher d) throws IOException {
		super(sc, d, 8192 * 2, 8192 * 2);

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
		if (!decrypt(data)) {
			log.warn("Wrong checksum from " + this);
			return false;
		}

		AionClientPacket pck = AionPacketHandlerFactory.handle(data, this);

		// Execute packet only if packet exists and read was ok.
		if (pck != null && pck.read())
			processor.executePacket(pck);

		return true;
	}

	/**
	 * This method will be called by Dispatcher, and will be repeated till return false.
	 * 
	 * @param data
	 * @return True if data was written to buffer, False indicating that there are not any more data to write.
	 */
	@Override
	protected final synchronized boolean writeData(ByteBuffer data) {
		AionServerPacket packet = sendMsgQueue.pollFirst();

		if (packet == null) {
			return false;
		}

		packet.setBuf(data);
		packet.write(this);

		return true;
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
	protected final void onServerClose() {
		// TODO mb some packet should be send to client before closing?
		close( /* packet */);
	}

	/**
	 * Decrypt packet.
	 * 
	 * @param buf
	 * @return true if success
	 */
	private boolean decrypt(ByteBuffer buf) {
		int size = buf.remaining();
		int offset = buf.arrayOffset() + buf.position();
		return cryptEngine.decrypt(buf.array(), offset, size);
	}

	/**
	 * Encrypt packet.
	 * 
	 * @param buf
	 * @return encrypted packet size.
	 */
	public final int encrypt(ByteBuffer buf) {
		int size = buf.limit() - 2;
		int offset = buf.arrayOffset() + buf.position();
		return cryptEngine.encrypt(buf.array(), offset, size);
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
	protected void initialized() {
		state = State.CONNECTED;
		log.info("Connection attempt from: " + getIP());
		encryptedRSAKeyPair = KeyGen.getEncryptedRSAKeyPair();
		SecretKey blowfishKey = KeyGen.generateBlowfishKey();

		cryptEngine = new CryptEngine();
		cryptEngine.updateKey(blowfishKey.getEncoded());

		sendPacket(new SM_INIT(this, blowfishKey));
	}
}
