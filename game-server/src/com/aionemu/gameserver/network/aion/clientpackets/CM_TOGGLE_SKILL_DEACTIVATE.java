package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STANCE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class CM_TOGGLE_SKILL_DEACTIVATE extends AionClientPacket {

	private int skillId;

	public CM_TOGGLE_SKILL_DEACTIVATE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		skillId = readUH();
		readH();
		readH();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		player.getEffectController().removeEffect(skillId);

		if (player.getController().getStanceSkillId() == skillId) {
			PacketSendUtility.sendPacket(player, new SM_PLAYER_STANCE(player, 0));
			player.getController().startStance(0);
		}
	}
}
