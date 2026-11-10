package com.aionemu.loginserver;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.network.gameserver.GsAuthResponse;
import com.aionemu.loginserver.network.gameserver.GsConnection;

import io.netty.channel.embedded.EmbeddedChannel;

/**
 * The packets of game servers are executed concurrently, and so are those of the clients that read the state of the game servers.
 */
public class GameServerTableTest {

	private static final byte SERVER_ID = 1;
	private static final String PASSWORD = "password";

	private GameServerInfo server;
	private final List<EmbeddedChannel> channels = new ArrayList<>();

	@BeforeEach
	public void loadGameServers() throws Exception {
		server = new GameServerInfo(SERVER_ID, "*", PASSWORD);
		Field field = GameServerTable.class.getDeclaredField("gameservers");
		field.setAccessible(true);
		field.set(null, Map.of(SERVER_ID, server));
	}

	@AfterEach
	public void close() {
		channels.forEach(EmbeddedChannel::finishAndReleaseAll);
	}

	private GsConnection connect() {
		InetSocketAddress address = new InetSocketAddress("127.0.0.1", 50000);
		EmbeddedChannel channel = new EmbeddedChannel() {

			@Override
			protected SocketAddress remoteAddress0() {
				return address;
			}
		};
		channels.add(channel);
		return new GsConnection(channel);
	}

	private static GsAuthResponse register(GsConnection connection) {
		return GameServerTable.registerGameServer(connection, SERVER_ID, PASSWORD, new byte[] { 127, 0, 0, 1 }, 7777, (byte) 0, 100);
	}

	private static Account account(int id) {
		Account account = new Account();
		account.setId(id);
		return account;
	}

	@Test
	public void onlyOneOfTwoGameServersThatRegisterTheSameIdAtOnceGetsIt() throws Exception {
		try (ExecutorService threads = Executors.newFixedThreadPool(2)) {
			for (int i = 0; i < 2000; i++) {
				server.setConnection(null);
				CyclicBarrier start = new CyclicBarrier(2);
				List<Future<GsAuthResponse>> responses = new ArrayList<>();
				for (int j = 0; j < 2; j++) {
					GsConnection connection = connect();
					responses.add(threads.submit(() -> {
						start.await();
						return register(connection);
					}));
				}
				List<GsAuthResponse> results = List.of(responses.get(0).get(), responses.get(1).get());
				assertTrue(results.contains(GsAuthResponse.AUTHED) && results.contains(GsAuthResponse.ALREADY_REGISTERED), "run " + i + ": " + results);
			}
		}
	}

	@Test
	public void anAccountOfAGameServerThatJustDisconnectedIsNotKicked() {
		assertEquals(GsAuthResponse.AUTHED, register(connect()));
		server.addAccountToGameServer(account(42));
		server.setConnection(null); // the state between the two steps of the disconnect of a game server
		assertFalse(GameServerTable.kickAccountFromGameServer(42, true));
	}
}
