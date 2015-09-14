package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.player.BlockedPlayer;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SocialService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Ben
 */
public class CM_BLOCK_DEL extends AionClientPacket {

	private static Logger log = LoggerFactory.getLogger(CM_BLOCK_DEL.class);

	private String targetName;

	/**
	 * @param opcode
	 */
	public CM_BLOCK_DEL(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		targetName = readS();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();

		if (activePlayer.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANAGE_FRIENDS) {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(activePlayer,
				"Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}

		BlockedPlayer target = activePlayer.getBlockList().getBlockedPlayer(targetName);
		if (target == null) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_BUDDYLIST_NOT_IN_LIST);
		} else {
			if (!SocialService.deleteBlockedUser(activePlayer, target.getObjId())) {
				log.debug("Could not unblock " + targetName + " from " + activePlayer.getName() + " blocklist. Check database setup.");
			}
		}
	}
}
