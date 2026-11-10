package com.aionemu.loginserver.network.aion;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aionemu.loginserver.GameServerTable;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.network.ncrypt.KeyGen;

import io.netty.channel.embedded.EmbeddedChannel;

/**
 * A client that chose a game server stays registered on the login server after it disconnects, until the game server asks for its session key.
 * These tests cover the clients that never get there.
 */
public class LoginSessionTest {

	private final Account account = new Account();
	private final List<EmbeddedChannel> channels = new ArrayList<>();
	private final long defaultJoinTimeoutMillis = LoginConnection.joinTimeoutMillis;

	@BeforeAll
	public static void init() throws Exception {
		KeyGen.init();
		Field field = GameServerTable.class.getDeclaredField("gameservers");
		field.setAccessible(true);
		field.set(null, Map.of());
	}

	@BeforeEach
	public void createAccount() {
		account.setId(42);
	}

	@AfterEach
	public void close() {
		LoginConnection.joinTimeoutMillis = defaultJoinTimeoutMillis;
		for (EmbeddedChannel channel : channels) {
			AccountController.removeAccountOnLS(connection(channel));
			channel.finishAndReleaseAll();
		}
	}

	private EmbeddedChannel connect() throws Exception {
		InetSocketAddress client = new InetSocketAddress("127.0.0.1", 50000);
		EmbeddedChannel channel = new EmbeddedChannel(false, false) {

			@Override
			protected SocketAddress remoteAddress0() {
				return client;
			}
		};
		LoginConnection.initChannel(channel, Runnable::run);
		channel.register();
		channels.add(channel);
		return channel;
	}

	private static LoginConnection connection(EmbeddedChannel channel) {
		return channel.attr(io.netty.util.AttributeKey.<LoginConnection> valueOf(com.aionemu.commons.network.netty.NettyConnection.class, "connection")).get();
	}

	/**
	 * @return The channel of a client that logged in and chose a game server.
	 */
	private EmbeddedChannel loginAndChooseGameServer() throws Exception {
		EmbeddedChannel channel = connect();
		assertEquals(AionAuthResponse.STR_L2AUTH_S_ALL_OK, AccountController.registerOnLS(account, connection(channel)));
		connection(channel).setJoinedGs();
		return channel;
	}

	@Test
	public void aClientThatLeftForAGameServerItNeverReachedDoesNotRefuseTheNextLogin() throws Exception {
		EmbeddedChannel first = loginAndChooseGameServer();
		first.close();
		assertTrue(AccountController.isRegisteredOnLS(connection(first)), "the game server may still ask for the session key");
		EmbeddedChannel second = connect();
		assertEquals(AionAuthResponse.STR_L2AUTH_S_ALL_OK, AccountController.registerOnLS(account, connection(second)));
		assertTrue(AccountController.isRegisteredOnLS(connection(second)));
	}

	@Test
	public void aClientThatIsStillConnectedRefusesTheNextLoginAndGetsKicked() throws Exception {
		EmbeddedChannel first = loginAndChooseGameServer();
		EmbeddedChannel second = connect();
		assertEquals(AionAuthResponse.STR_L2AUTH_S_ALREADY_LOGIN, AccountController.registerOnLS(account, connection(second)));
		assertFalse(first.isOpen());
		assertFalse(AccountController.isRegisteredOnLS(connection(first)));
		assertFalse(AccountController.isRegisteredOnLS(connection(second)));
	}

	@Test
	public void forgetsAClientThatDoesNotReachItsGameServerInTime() throws Exception {
		LoginConnection.joinTimeoutMillis = 50;
		EmbeddedChannel channel = loginAndChooseGameServer();
		channel.close();
		long deadline = System.nanoTime() + 5_000_000_000L;
		while (AccountController.isRegisteredOnLS(connection(channel)) && System.nanoTime() < deadline)
			Thread.sleep(10);
		assertFalse(AccountController.isRegisteredOnLS(connection(channel)));
	}

	@Test
	public void theTimeoutOfAnOlderConnectionLeavesANewerLoginAlone() throws Exception {
		LoginConnection.joinTimeoutMillis = 50;
		EmbeddedChannel first = loginAndChooseGameServer();
		first.close();
		EmbeddedChannel second = connect();
		assertEquals(AionAuthResponse.STR_L2AUTH_S_ALL_OK, AccountController.registerOnLS(account, connection(second)));
		Thread.sleep(300);
		assertTrue(AccountController.isRegisteredOnLS(connection(second)));
	}

	@Test
	public void keepsAClientThatChoseAGameServerWhileItIsConnected() throws Exception {
		LoginConnection.joinTimeoutMillis = 50;
		EmbeddedChannel channel = loginAndChooseGameServer();
		Thread.sleep(300);
		assertTrue(AccountController.isRegisteredOnLS(connection(channel)));
	}
}
