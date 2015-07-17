package com.aionemu.chatserver.network.gameserver.clientpackets;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.network.gameserver.GsClientPacket;
import com.aionemu.chatserver.network.gameserver.GsConnection;
import com.aionemu.chatserver.network.gameserver.serverpackets.SM_PLAYER_AUTH_RESPONSE;
import com.aionemu.chatserver.service.ChatService;

/**
 * @author ATracer
 */
public class CM_PLAYER_AUTH extends GsClientPacket
{
	private static final Logger log = LoggerFactory.getLogger(CM_PLAYER_AUTH.class);
	private int playerId;
	private String playerLogin;
	private String nick;

	public CM_PLAYER_AUTH(ByteBuffer buf, GsConnection connection)
	{
		super(buf, connection, 0x01);
	}

	@Override
	protected void readImpl()
	{
		playerId = readD();
		playerLogin = readS();
		nick = readS();
	}

	@Override
	protected void runImpl()
	{
		ChatClient chatClient = null;
		try
		{
			chatClient = ChatService.getInstance().registerPlayer(playerId, playerLogin, nick);
		}
		catch (NoSuchAlgorithmException e)
		{
			log.error("Error registering player on ChatServer: " + e.getMessage());
		}
		catch (UnsupportedEncodingException e)
		{
			log.error("Error registering player on ChatServer: " + e.getMessage());
		}

		if (chatClient != null)
		{
			getConnection().sendPacket(new SM_PLAYER_AUTH_RESPONSE(chatClient));
		}
		else
		{
			log.info("Player was not authed " + playerId);
		}
	}
}
