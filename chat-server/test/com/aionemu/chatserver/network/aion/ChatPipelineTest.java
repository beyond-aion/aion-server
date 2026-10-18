package com.aionemu.chatserver.network.aion;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.Race;
import com.aionemu.chatserver.service.ChatService;
import com.aionemu.commons.network.netty.NettyPipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;

/**
 * Runs client traffic through the chat server pipeline. The frames are built after the protocol description, independently of the server's
 * packets: client frames are size, 16-bit opcode, sequence number and body, server frames size, 16-bit opcode and body.
 */
public class ChatPipelineTest {

	private static final int PLAYER_ID = 123456;
	private static final String NAME = "Tester";
	private static final String ACCOUNT = "tester";
	private static final String CHANNEL = "@\u0001trade_Housing_barrack\u00011.0.AION.KOR";

	private EmbeddedChannel channel;
	private int sequence;

	@BeforeEach
	public void connect() throws Exception {
		InetSocketAddress client = new InetSocketAddress("127.0.0.1", 50000);
		channel = new EmbeddedChannel(false, false) {

			@Override
			protected SocketAddress remoteAddress0() {
				return client;
			}
		};
		NettyPipeline.init(channel, new AionConnection(channel, ImmediateEventExecutor.INSTANCE), 8192 * 2);
		channel.register();
	}

	@AfterEach
	public void close() {
		channel.finishAndReleaseAll();
	}

	private void send(int opcode, ByteBuf body) {
		ByteBuf frame = Unpooled.buffer().writeShortLE(0).writeShortLE(opcode).writeShortLE(sequence++).writeBytes(body);
		frame.setShortLE(0, frame.readableBytes());
		channel.writeInbound(frame);
		channel.runPendingTasks();
	}

	/**
	 * @return The body of the next server packet, after checking its size header and opcode.
	 */
	private ByteBuf receive(int opcode) {
		channel.runPendingTasks();
		ByteBuf frame = channel.readOutbound();
		assertNotNull(frame, "no packet was sent");
		assertEquals(frame.readableBytes(), frame.readUnsignedShortLE());
		assertEquals(opcode, frame.readUnsignedShortLE());
		return frame;
	}

	private static void writeS(ByteBuf buf, String text) {
		buf.writeShortLE(text.length()).writeBytes(text.getBytes(StandardCharsets.UTF_16LE));
	}

	private void login() throws Exception {
		ChatClient chatClient = ChatService.getInstance().registerPlayer(PLAYER_ID, ACCOUNT, NAME, Race.ELYOS, (byte) 0);
		ByteBuf body = Unpooled.buffer().writeIntLE(1); // msgIndex
		writeS(body, "AION");
		body.writeIntLE(27).writeLongLE(1).writeLongLE(PLAYER_ID).writeLongLE(0);
		writeS(body, NAME + "@1");
		writeS(body, ACCOUNT);
		body.writeShortLE(chatClient.getToken().length).writeBytes(chatClient.getToken());
		send(0x4005, body);
		ByteBuf result = receive(0x4002);
		assertEquals(1, result.readIntLE(), "msgIndex");
		assertEquals(0, result.readShortLE(), "result");
		result.readShortLE(); // start value of the sequence numbers
		assertFalse(result.isReadable());
	}

	private int joinChannel(int msgIndex) {
		ByteBuf body = Unpooled.buffer().writeIntLE(msgIndex).writeZero(16);
		writeS(body, CHANNEL);
		body.writeIntLE(0);
		send(0x4010, body);
		ByteBuf result = receive(0x4011);
		assertEquals(msgIndex, result.readIntLE(), "msgIndex");
		assertEquals(0, result.readShortLE(), "result");
		int channelId = result.readIntLE();
		assertFalse(result.isReadable());
		return channelId;
	}

	@Test
	public void answersTheVersionInfoWithTheClientsMessageIndex() {
		send(0x4030, Unpooled.buffer().writeIntLE(7).writeIntLE(1).writeIntLE(2));
		ByteBuf result = receive(0x4031);
		assertEquals(7, result.readIntLE(), "msgIndex");
		assertEquals(0, result.readShortLE(), "result");
		assertFalse(result.isReadable());
	}

	@Test
	public void logsInJoinsAChannelAndChats() throws Exception {
		login();
		int channelId = joinChannel(5);
		String text = "hello";
		ByteBuf body = Unpooled.buffer().writeZero(16).writeIntLE(channelId).writeByte(0);
		writeS(body, text);
		send(0x0018, body);
		ByteBuf message = receive(0x001A);
		assertEquals(PLAYER_ID, message.readLongLE(), "recipient");
		assertEquals(channelId, message.readIntLE());
		assertEquals(PLAYER_ID, message.readLongLE(), "sender");
		assertEquals(0, message.readByte(), "user type");
		message.skipBytes(message.readUnsignedShortLE() * 2); // chat name
		int length = message.readUnsignedShortLE();
		assertEquals(text, message.readCharSequence(length * 2, StandardCharsets.UTF_16LE).toString());
		assertFalse(message.isReadable());
		assertTrue(channel.isOpen());
	}

	@Test
	public void readsTheOtherPacketsOfAnAuthenticatedClient() throws Exception {
		login();
		int channelId = joinChannel(1);
		send(0x002C, Unpooled.buffer().writeIntLE(1).writeByte(0).writeIntLE(65).writeZero(135));
		send(0x00FF, Unpooled.buffer().writeZero(16));
		send(0x0012, Unpooled.buffer().writeZero(16).writeIntLE(channelId));
		assertNull(channel.readOutbound());
		assertTrue(channel.isOpen());
	}
}
