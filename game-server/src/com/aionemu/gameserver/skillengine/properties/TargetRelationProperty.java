package com.aionemu.gameserver.skillengine.properties;

import java.util.Iterator;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer
 */
public class TargetRelationProperty {

	public static boolean set(final Skill skill, Properties properties) {

		TargetRelationAttribute value = properties.getTargetRelation();
		List<Creature> targetsList = skill.getEffectedList();
		Creature source = skill.getEffector();

		switch (value) {
			case ALL:
				break;
			case ENEMY:
				if (!DataManager.MATERIAL_DATA.isMaterialSkill(skill.getSkillId()))
					targetsList.removeIf(target -> !source.isEnemy(target));
				break;
			case FRIEND:
				if (!DataManager.MATERIAL_DATA.isMaterialSkill(skill.getSkillId()))
					targetsList.removeIf(target -> source.isEnemy(target) || !isBuffAllowed(source, target));

				if (targetsList.isEmpty()) {
					skill.setFirstTarget(skill.getEffector());
					targetsList.add(skill.getEffector());
				} else {
					skill.setFirstTarget(targetsList.get(0));
				}
				break;
			case MYPARTY:
				for (Iterator<Creature> iter = targetsList.iterator(); iter.hasNext();) {
					Creature target = iter.next();

					if (source.getMaster() instanceof Player sourcePlayer && target instanceof Player targetPlayer) {
						if (isBuffAllowed(source, targetPlayer)) {
							if (targetPlayer.equals(sourcePlayer))
								continue;
							int teamId = sourcePlayer.getCurrentTeamId();
							if (teamId > 0 && teamId == targetPlayer.getCurrentTeamId() && !sourcePlayer.isEnemy(targetPlayer))
								continue;
						}
					}
					iter.remove();
				}

				if (!targetsList.isEmpty()) {
					skill.setFirstTarget(targetsList.get(0));
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
