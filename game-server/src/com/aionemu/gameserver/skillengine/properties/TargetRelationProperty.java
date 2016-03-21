package com.aionemu.gameserver.skillengine.properties;

import java.util.Iterator;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Servant;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer
 */
public class TargetRelationProperty {

	/**
	 * @param skill
	 * @param properties
	 * @return
	 */
	public static boolean set(final Skill skill, Properties properties) {

		TargetRelationAttribute value = properties.getTargetRelation();

		final List<Creature> targetsList = skill.getEffectedList();
		boolean isMaterialSkill = DataManager.MATERIAL_DATA.isMaterialSkill(skill.getSkillId());
		Creature source = skill.getEffector();

		switch (value) {
			case ALL:
				break;
			case ENEMY:
				for (Iterator<Creature> iter = targetsList.iterator(); iter.hasNext();) {
					Creature target = iter.next();

					if (target instanceof Summon) {
						Summon summon = (Summon) target;
						if (summon.isEnemy(source) && summon.getMaster().isEnemy(source)) {
							continue;
						}
					} else if (source.isEnemy(target) || isMaterialSkill)
						continue;

					iter.remove();
				}
				break;
			case FRIEND:
				for (Iterator<Creature> iter = targetsList.iterator(); iter.hasNext();) {
					Creature target = iter.next();

					if (!source.isEnemy(target) && isBuffAllowed(source, target) || isMaterialSkill)
						continue;

					iter.remove();
				}

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

					if (target instanceof Player) {
						Player targetPlayer = (Player) target;
						if (isBuffAllowed(source, targetPlayer)) {
							Player sourcePlayer;
							if (source instanceof Servant) {
								sourcePlayer = (Player) ((Servant) source).getMaster();
							} else {
								sourcePlayer = (Player) source;
							}

							if (sourcePlayer.isInAlliance2() && targetPlayer.isInAlliance2()) {
								if (!sourcePlayer.isEnemy(targetPlayer)
									&& sourcePlayer.getPlayerAlliance2().equals(targetPlayer.getPlayerAlliance2())) {
									continue;
								}
							} else if (sourcePlayer.isInGroup2() && targetPlayer.isInGroup2()) {
								if (!sourcePlayer.isEnemy(targetPlayer)
									&& sourcePlayer.getPlayerGroup2().getTeamId().equals(targetPlayer.getPlayerGroup2().getTeamId())) {
									continue;
								}
							} else if (targetPlayer.equals(sourcePlayer)) {
								continue;
							}
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
	 * @param source
	 * @param target
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
