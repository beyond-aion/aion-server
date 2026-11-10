package com.aionemu.gameserver.network.chatserver;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import io.netty.channel.embedded.EmbeddedChannel;

/**
 * The chat server link is dropped by another thread than those that use it.
 */
public class ChatServerTest {

	private final ChatServer chatServer = ChatServer.getInstance();

	@Test
	public void usingTheLinkWhileItIsDroppedAndRestoredThrowsNothing() throws Exception {
		Field field = ChatServer.class.getDeclaredField("csCon");
		field.setAccessible(true);
		InetSocketAddress address = new InetSocketAddress("127.0.0.1", 50000);
		EmbeddedChannel channel = new EmbeddedChannel() {

			@Override
			protected SocketAddress remoteAddress0() {
				return address;
			}
		};
		ChatServerConnection csCon = new ChatServerConnection(channel);
		csCon.setState(ChatServerConnection.State.AUTHED);
		csCon.close(); // packets sent to it are dropped from now on, so the channel is not written to by several threads
		AtomicBoolean done = new AtomicBoolean();
		try (ExecutorService threads = Executors.newFixedThreadPool(4)) {
			List<Future<?>> users = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				users.add(threads.submit(() -> {
					while (!done.get()) {
						chatServer.isUp();
						chatServer.sendPlayerGagPacket(1, 1000);
					}
				}));
			}
			try {
				for (int i = 0; i < 300_000; i++) {
					field.set(chatServer, null);
					field.set(chatServer, csCon);
				}
			} finally {
				done.set(true);
				field.set(chatServer, null);
			}
			for (Future<?> user : users)
				user.get(); // rethrows what the thread ran into
		} finally {
			channel.finishAndReleaseAll();
		}
	}

	@Test
	public void thePublicAddressIsOneValue() {
		chatServer.setPublicAddress(new byte[] { 10, 0, 0, 1 }, 10241);
		ChatServer.PublicAddress address = chatServer.getPublicAddress();
		chatServer.disconnect(); // forgets the address
		assertEquals(4, address.ip().length);
		assertEquals(10241, address.port());
		assertEquals(0, chatServer.getPublicAddress().ip().length);
	}
}
