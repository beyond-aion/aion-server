package com.aionemu.gameserver.skillengine.properties;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.properties.Properties.CastState;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
public class FirstTargetRangeProperty {

	/**
	 * @param skill
	 * @param properties
	 */
	public static boolean set(Skill skill, Properties properties, CastState castState) {
		float firstTargetRange = properties.getFirstTargetRange();
		if (!skill.isFirstTargetRangeCheck())
			return true;

		Creature effector = skill.getEffector();
		Creature firstTarget = skill.getFirstTarget();

		if (firstTarget == null)
			return false;

		if (firstTarget.equals(effector)) {
			return true;
		}

		if (!castState.isCastStart() && !(effector instanceof Player)) { // NPCs don't cancel skills once started, could be abused -> no range or geo to check
			return true;
		}

		// on end cast check add revision distance value
		if (!castState.isCastStart())
			firstTargetRange += properties.getRevisionDistance();

		// Add Weapon Range to distance
		if (properties.isAddWeaponRange()) {
			firstTargetRange += skill.getEffector().getGameStats().getAttackRange().getCurrent() / 1000f;
		}

		if (!MathUtil.isInAttackRange(effector, firstTarget, firstTargetRange + 2)
			&& !firstTarget.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_MOVE_STATE)) {
			if (effector instanceof Player) {
				PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_ATTACK_TOO_FAR_FROM_TARGET());
			}
			return false;
		}

		// TODO check for all targets too
		// Cannon exception
		if (effector.getTransformModel().getModelId() != 284867) {
			if (!GeoService.getInstance().canSee(effector, firstTarget)) {
				if (effector instanceof Player) {
					PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_SKILL_OBSTACLE());
				}
				return false;
			}
		}
		return true;
	}

}
