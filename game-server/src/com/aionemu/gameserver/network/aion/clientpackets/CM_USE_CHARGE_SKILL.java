package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author Cheatkiller
 */
public class CM_USE_CHARGE_SKILL extends AionClientPacket {

	public CM_USE_CHARGE_SKILL(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Skill chargeCastingSkill = player.getCastingSkill();
		if (chargeCastingSkill == null || chargeCastingSkill.getChargeTimes() == null) {
			return;
		}
		long time = System.currentTimeMillis() - chargeCastingSkill.getCastStartTime();
		int i = 0;
		for(; i < chargeCastingSkill.getChargeTimes().length; i++) {
			if (time < chargeCastingSkill.getChargeTimes()[i])
				break;
			time -= chargeCastingSkill.getChargeTimes()[i];
		}
		player.getController().useChargeSkill(chargeCastingSkill.getChargeSkillList().get(i).getId(), chargeCastingSkill.getSkillLevel(),
			chargeCastingSkill.getHitTime(), chargeCastingSkill.getFirstTarget());
		chargeCastingSkill.cancelCast();
		chargeCastingSkill.getChargeSkillList().clear();
	}

	@Override
	protected void readImpl() {
	}

}
