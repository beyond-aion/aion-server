package com.aionemu.gameserver.skillengine.properties;

import java.util.Iterator;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author ATracer
 */
public class TargetRelationProperty {

	public static boolean set(Properties properties, Properties.ValidationResult result, Creature effector, SkillTemplate skillTemplate) {
		TargetRelationAttribute value = properties.getTargetRelation();
		switch (value) {
			case ALL:
				break;
			case ENEMY:
				if (!DataManager.MATERIAL_DATA.isMaterialSkill(skillTemplate.getSkillId()))
					result.getTargets().removeIf(target -> !effector.isEnemy(target));
				break;
			case FRIEND:
				if (!DataManager.MATERIAL_DATA.isMaterialSkill(skillTemplate.getSkillId()))
					result.getTargets().removeIf(target -> effector.isEnemy(target) || !isBuffAllowed(effector, target));

				if (result.getTargets().isEmpty()) {
					result.setFirstTarget(effector);
					result.getTargets().add(effector);
				} else {
					result.setFirstTarget(result.getTargets().getFirst());
				}
				break;
			case MYPARTY:
				for (Iterator<Creature> iter = result.getTargets().iterator(); iter.hasNext();) {
					Creature target = iter.next();
					if (effector.getMaster() instanceof Player sourcePlayer && isBuffAllowed(effector, target)) {
						if (target.getMaster().equals(sourcePlayer))
							continue;
						if (target.getMaster() instanceof Player targetPlayer) {
							int teamId = sourcePlayer.getCurrentTeamId();
							if (teamId > 0 && teamId == targetPlayer.getCurrentTeamId() && !sourcePlayer.isEnemy(targetPlayer))
								continue;
						}
					}
					iter.remove();
				}

				if (!result.getTargets().isEmpty()) {
					result.setFirstTarget(result.getTargets().getFirst());
				}
				break;
		}

		return true;
	}

	/**
	 * @return true = allow buff, false = deny buff
	 */
	public static boolean isBuffAllowed(Creature source, Creature target) {
		if (source == null || target == null) {
			return false;
		}

		if (target instanceof SiegeNpc) {
			switch (((SiegeNpc) target).getObjectTemplate().getAbyssNpcType()) {
				case ARTIFACT:
				case ARTIFACT_EFFECT_CORE:
				case DOOR:
				case DOORREPAIR:
					return false;
			}
		}

		return isSameAreaType(source, target);
	}

	public static boolean isSameAreaType(Creature source, Creature target) {
		return source.isInsidePvPZone() == target.isInsidePvPZone();
	}

}
