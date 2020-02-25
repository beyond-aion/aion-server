package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillSubType;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author dragoon112, Neon
 */
public class CM_REMOVE_ALTERED_STATE extends AionClientPacket {

	private int skillId;

	public CM_REMOVE_ALTERED_STATE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		skillId = readUH();
		readC();
		readC(); // seen 1 with skillId 3573
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Effect effect = player.getEffectController().findBySkillId(skillId);
		if (effect != null) {
			if (effect.getSkillSubType() == SkillSubType.DEBUFF) {
				AuditLogger.log(player, "tried to remove a debuff: " + skillId + " " + effect.getSkillName() + " (effector: "
					+ effect.getEffector() + ")");
			} else {
				effect.endEffect();
			}
		}
	}

}
