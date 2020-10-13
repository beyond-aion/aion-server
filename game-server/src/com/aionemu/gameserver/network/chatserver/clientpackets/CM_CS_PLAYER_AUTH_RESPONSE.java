package com.aionemu.gameserver.network.chatserver.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CHAT_INIT;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.network.chatserver.CsClientPacket;
import com.aionemu.gameserver.services.ban.ChatBanService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
public class CM_CS_PLAYER_AUTH_RESPONSE extends CsClientPacket {

	/**
	 * Player for which authentication was performed
	 */
	private int playerId;
	/**
	 * Token will be sent to client
	 */
	private byte[] token;

	/**
	 * @param opcode
	 */
	public CM_CS_PLAYER_AUTH_RESPONSE(int opcode) {
		super(opcode);
	}

	@Override
	protected void readImpl() {
		playerId = readD();
		int tokenLenght = readUC();
		token = readB(tokenLenght);
	}

	@Override
	protected void runImpl() {
		if (ChatServer.getInstance().isUp()) {
			Player player = World.getInstance().getPlayer(playerId);
			if (player != null) {
				PacketSendUtility.sendPacket(player, new SM_CHAT_INIT(token));
				if (ChatBanService.isBanned(player))
					ChatServer.getInstance().sendPlayerGagPacket(player.getObjectId(), ChatBanService.getBanMinutes(player) * 60000L);
			}
		}
	}
}
