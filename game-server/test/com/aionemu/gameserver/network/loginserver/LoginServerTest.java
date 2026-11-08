package com.aionemu.gameserver.network.loginserver;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_L2AUTH_LOGIN_CHECK;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_ACCOUNT_AUTH;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_ACCOUNT_DISCONNECTED;

import io.netty.channel.embedded.EmbeddedChannel;

/**
 * Checks which client gets the login server's answer to a login request. The answer only names the account, so clients that claim someone else's
 * account must neither receive it nor make the login server forget the account. The channels have no pipeline, so the sent packets can be read
 * as objects.
 */
public class LoginServerTest {

	private static final int ACCOUNT_ID = 42;

	private final LoginServer loginServer = LoginServer.getInstance();
	private EmbeddedChannel lsChannel;
	private final List<EmbeddedChannel> clientChannels = new ArrayList<>();

	@BeforeEach
	public void connectLoginServer() throws Exception {
		lsChannel = embeddedChannel();
		LoginServerConnection lsCon = new LoginServerConnection(lsChannel);
		lsCon.setState(LoginServerConnection.State.AUTHED);
		Field field = LoginServer.class.getDeclaredField("lsCon");
		field.setAccessible(true);
		field.set(loginServer, lsCon);
	}

	@AfterEach
	public void disconnect() {
		loginServer.disconnect();
		lsChannel.finishAndReleaseAll();
		clientChannels.forEach(EmbeddedChannel::finishAndReleaseAll);
	}

	private static EmbeddedChannel embeddedChannel() {
		InetSocketAddress address = new InetSocketAddress("127.0.0.1", 50000);
		return new EmbeddedChannel() {

			@Override
			protected SocketAddress remoteAddress0() {
				return address;
			}
		};
	}

	private AionConnection client() {
		EmbeddedChannel channel = embeddedChannel();
		clientChannels.add(channel);
		return new AionConnection(channel);
	}

	private static EmbeddedChannel channel(AionConnection client) throws Exception {
		Field field = client.getClass().getSuperclass().getDeclaredField("channel");
		field.setAccessible(true);
		return (EmbeddedChannel) field.get(client);
	}

	private List<Object> sentToLoginServer() {
		List<Object> packets = new ArrayList<>();
		for (Object packet; (packet = lsChannel.readOutbound()) != null;)
			packets.add(packet);
		return packets;
	}

	private void login(AionConnection client, int loginOk) {
		loginServer.registerLoginRequest(ACCOUNT_ID, client, loginOk, 2, 3);
		loginServer.authenticateClient(client);
	}

	private void answer(boolean result) {
		loginServer.accountAuthenticationResponse(ACCOUNT_ID, result ? "account" : null, result, 0, null, (byte) 0, (byte) 0, "");
	}

	@Test
	public void aRejectedRequestDoesNotLogOutTheAccount() throws Exception {
		AionConnection forger = client();
		login(forger, 666);
		assertInstanceOf(SM_ACCOUNT_AUTH.class, sentToLoginServer().getFirst());
		answer(false);
		assertTrue(sentToLoginServer().stream().noneMatch(SM_ACCOUNT_DISCONNECTED.class::isInstance),
			"the login server must not be told to forget an account it didn't accept for this client");
		assertInstanceOf(SM_L2AUTH_LOGIN_CHECK.class, channel(forger).readOutbound());
		assertTrue(forger.isClosing());
	}

	@Test
	public void checksOneRequestPerAccountAtATimeAndAnswersTheClientThatSentIt() throws Exception {
		AionConnection forger = client();
		AionConnection owner = client();
		login(forger, 666);
		login(owner, 1);
		assertEquals(1, sentToLoginServer().size(), "the owner's request must wait for the answer to the first one");
		answer(false);
		assertTrue(forger.isClosing(), "the first answer belongs to the first request");
		assertFalse(owner.isClosing());
		List<Object> sent = sentToLoginServer();
		assertEquals(1, sent.size(), "the owner's request must be checked after the first answer");
		assertInstanceOf(SM_ACCOUNT_AUTH.class, sent.getFirst());
	}

	@Test
	public void aWaitingRequestOfAClosedClientIsDropped() throws Exception {
		AionConnection first = client();
		AionConnection second = client();
		AionConnection third = client();
		login(first, 1);
		login(second, 2);
		login(third, 3);
		sentToLoginServer();
		channel(second).close();
		loginServer.onDisconnect(second);
		answer(false);
		assertEquals(1, sentToLoginServer().size());
		answer(false);
		assertTrue(third.isClosing(), "the second answer belongs to the third client");
	}

	@Test
	public void aSecondRequestOfTheSameClientClosesIt() {
		AionConnection client = client();
		loginServer.registerLoginRequest(ACCOUNT_ID, client, 1, 2, 3);
		loginServer.registerLoginRequest(ACCOUNT_ID + 1, client, 1, 2, 3);
		assertTrue(client.isClosing());
	}

	@Test
	public void anAcceptedAnswerForAClientThatLeftLogsTheAccountOutAgain() throws Exception {
		AionConnection client = client();
		login(client, 1);
		sentToLoginServer();
		channel(client).close();
		loginServer.onDisconnect(client);
		answer(true);
		assertTrue(sentToLoginServer().stream().anyMatch(SM_ACCOUNT_DISCONNECTED.class::isInstance),
			"the login server moved the account to this game server, so it must learn that nobody plays it");
	}
}
