package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.skillengine.condition.SkillChargeCondition;
import com.aionemu.gameserver.skillengine.model.*;

/**
 * @author Cheatkiller
 */
public class CM_USE_CHARGE_SKILL extends AionClientPacket {

	public CM_USE_CHARGE_SKILL(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
	}

	@SuppressWarnings("lossy-conversions")
	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Skill chargeCastingSkill = player.getCastingSkill();
		if (chargeCastingSkill == null || chargeCastingSkill.getChargeTimes() == null) {
			return;
		}
		SkillChargeCondition chargeCondition = chargeCastingSkill.getSkillTemplate().getSkillChargeCondition();
		ChargeSkillEntry chargeSkill = DataManager.SKILL_CHARGE_DATA.getChargedSkillEntry(chargeCondition.getValue());
		long time = System.currentTimeMillis() - chargeCastingSkill.getCastStartTime();
		int index = 0;
		for (float chargeTime : chargeCastingSkill.getChargeTimes()) {
			if (time < chargeTime || ++index == chargeSkill.getSkills().size() - 1)
				break;
			time -= chargeTime;
		}
		player.getController().useChargeSkill(chargeSkill.getSkills().get(index).getId(), chargeCastingSkill.getSkillLevel(),
			chargeCastingSkill.getHitTime(), calculateAnimationTimeFor(player, chargeSkill, index), chargeCastingSkill.getFirstTarget());
		chargeCastingSkill.cancelCast();
	}

	private int calculateAnimationTimeFor(Player player, ChargeSkillEntry chargeSkill, int index) {
		SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(chargeSkill.getSkills().get(index).getId());
		Motion motion = skillTemplate.getMotion();
		MotionTime motionTime = DataManager.MOTION_DATA.getMotionTime(motion.getName());
		int animationTime = 0;
		if (motionTime != null) {
			WeaponTypeWrapper weapons = new WeaponTypeWrapper(player.getEquipment().getMainHandWeaponType(), player.getEquipment().getOffHandWeaponType());
			Times times = motionTime.getTimesFor(player.getRace(), player.getGender(), weapons, player.isInRobotMode(), index + 1);
			if (times != null) {
				float atkSpeed2 = ((float) player.getGameStats().getAttackSpeed().getCurrent() / (float) player.getGameStats().getAttackSpeed().getBase());
				animationTime = (int) (times.getMaxTime() * motion.getSpeed() * atkSpeed2 * 10);
			}
		}
		return animationTime;
	}
}
