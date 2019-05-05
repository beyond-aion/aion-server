package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.audit.AuditLogger;

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
		SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		if (skillTemplate == null || (!skillTemplate.isToggle() && !skillTemplate.isStance())) {
			AuditLogger.log(player, "tried to remove non-toggle skill effect (" + skillId + ") through CM_TOGGLE_SKILL_DEACTIVATE");
			return;
		}
		player.getEffectController().removeEffect(skillId);

		if (player.getController().getStanceSkillId() == skillId)
			player.getController().stopStance();
	}
}
