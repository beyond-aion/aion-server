package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionEmblem;
import com.aionemu.gameserver.model.team.legion.LegionEmblemType;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_SEND_EMBLEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Simple
 * @modified cura
 */
public class CM_LEGION_SEND_EMBLEM extends AionClientPacket {

	private int legionId;

	public CM_LEGION_SEND_EMBLEM(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		legionId = readD();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		if(activePlayer == null)
			return;
		if(activePlayer.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANAGE_LEGION) {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(activePlayer, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}
		Legion legion = LegionService.getInstance().getLegion(legionId);

		if (legion != null) {
			LegionEmblem legionEmblem = legion.getLegionEmblem();
			if (legionEmblem.getEmblemType() == LegionEmblemType.DEFAULT) {
				sendPacket(new SM_LEGION_SEND_EMBLEM(legionId, legionEmblem.getEmblemId(), legionEmblem.getColor_r(),
					legionEmblem.getColor_g(), legionEmblem.getColor_b(), legion.getLegionName(), legionEmblem.getEmblemType(), 0));
			}
			else {
				LegionService.getInstance().sendEmblemData(getConnection().getActivePlayer(), legionEmblem, legionId,
				legion.getLegionName());
			}
		}
	}
}
