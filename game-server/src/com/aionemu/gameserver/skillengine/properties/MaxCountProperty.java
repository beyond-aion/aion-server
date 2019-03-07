package com.aionemu.gameserver.skillengine.properties;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author MrPoke, Neon
 */
public class MaxCountProperty {

	public static boolean set(Skill skill, Properties properties) {
		TargetRangeAttribute value = properties.getTargetType();
		int maxCount = properties.getTargetMaxCount();
		if (properties.getFirstTarget() == FirstTargetAttribute.TARGET && value == TargetRangeAttribute.AREA) // firstTarget doesn't count on AREA skills (see skill 1245 or 16689)
			maxCount += 1;
		if (maxCount == 0 || skill.getEffectedList().size() <= maxCount)
			return true;

		switch (value) {
			case AREA:
			case PARTY:
			case PARTY_WITHPET:
				Creature firstTarget = skill.getFirstTarget();
				if (firstTarget == null)
					return false;

				Set<Creature> nearestCreatures = skill.getEffectedList().stream()
					.sorted(Comparator.comparingDouble(c -> PositionUtil.getDistance(firstTarget, c)))
					.limit(maxCount)
					.collect(Collectors.toSet());

				// rebuild effected list with correct number of creatures and their summons
				if (value == TargetRangeAttribute.PARTY_WITHPET) {
					for (Object creature : nearestCreatures.toArray()) {
						Creature summon = creature instanceof Player ? ((Player) creature).getSummon() : null;
						if (summon != null && skill.getEffectedList().contains(summon))
							nearestCreatures.add(summon);
					}
				}
				skill.getEffectedList().retainAll(nearestCreatures);
		}
		return true;
	}
}
