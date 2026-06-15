package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_BLOCK_RESPONSE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SocialService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.Util;

/**
 * @author Ben
 */
public class CM_BLOCK_ADD extends AionClientPacket {

	private String targetName;
	private String reason;

	public CM_BLOCK_ADD(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		targetName = readS();
		reason = readS();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		PlayerCommonData target = PlayerService.getOrLoadPlayerCommonData(Util.convertName(targetName));

		if (player.getName().equalsIgnoreCase(targetName))
			sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.CANT_BLOCK_SELF, targetName));
		else if (player.getBlockList().isFull())
			sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.LIST_FULL, targetName));
		else if (target == null)
			sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.TARGET_NOT_FOUND, targetName));
		else if (player.getFriendList().getFriend(target.getPlayerObjId()) != null)
			sendPacket(SM_SYSTEM_MESSAGE.STR_BLOCKLIST_NO_BUDDY());
		else if (player.getBlockList().contains(target.getPlayerObjId()))
			sendPacket(SM_SYSTEM_MESSAGE.STR_BLOCKLIST_ALREADY_BLOCKED());
		else
			SocialService.addBlockedUser(player, target, reason);
	}
}
