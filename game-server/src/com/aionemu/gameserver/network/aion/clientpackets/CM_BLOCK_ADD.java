package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_BLOCK_RESPONSE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SocialService;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.world.World;

/**
 * @author Ben
 */
public class CM_BLOCK_ADD extends AionClientPacket {

	private static Logger log = LoggerFactory.getLogger(CM_BLOCK_ADD.class);

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
		Player target = World.getInstance().getPlayer(Util.convertName(targetName));

		if (player.getName().equalsIgnoreCase(targetName))
			sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.CANT_BLOCK_SELF, targetName));
		else if (player.getBlockList().isFull())
			sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.LIST_FULL, targetName));
		else if (target == null)
			sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.TARGET_NOT_FOUND, targetName));
		else if (player.getFriendList().getFriend(target.getObjectId()) != null)
			sendPacket(SM_SYSTEM_MESSAGE.STR_BLOCKLIST_NO_BUDDY());
		else if (player.getBlockList().contains(target.getObjectId()))
			sendPacket(SM_SYSTEM_MESSAGE.STR_BLOCKLIST_ALREADY_BLOCKED());
		else if (!SocialService.addBlockedUser(player, target, reason))
			log.error("Failed to add " + target.getName() + " to the block list for " + player.getName() + " - check database setup.");
	}
}
