package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.ArmsfusionService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author zdead
 */
public class CM_BREAK_WEAPONS extends AionClientPacket {

	public CM_BREAK_WEAPONS(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	private int weaponToBreakUniqueId;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		readD();
		weaponToBreakUniqueId = readD();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (player.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_BREAK_WEAPONS) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(player, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}

		ArmsfusionService.breakWeapons(player, weaponToBreakUniqueId);
	}
}
