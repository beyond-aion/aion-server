package com.aionemu.gameserver.skillengine.properties;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author MrPoke
 */
public class MaxCountProperty {

	public static boolean set(final Skill skill, Properties properties) {
		TargetRangeAttribute value = properties.getTargetType();
		int maxCount = properties.getTargetMaxCount();
		if (maxCount == 0 || skill.getEffectedList().size() <= maxCount)
			return true;

		switch (value) {
			case AREA:
			case PARTY:
			case PARTY_WITHPET:
				Creature firstTarget = skill.getFirstTarget();
				if (firstTarget == null)
					return false;

				// filter out summons (we want nearest masters), then order by distance, limit to max count
				List<Creature> nearestCreatures = skill.getEffectedList().stream().filter(c -> !(c instanceof Summon))
					.sorted(Comparator.comparingDouble(c -> PositionUtil.getDistance(firstTarget, c))).limit(maxCount).collect(Collectors.toList());

				// rebuild effected list with correct number of creatures and their summons
				skill.getEffectedList().clear();
				nearestCreatures.forEach(c -> {
					skill.getEffectedList().add(c);
					if (value == TargetRangeAttribute.PARTY_WITHPET && c instanceof Player) {
						Summon summon = ((Player) c).getSummon();
						if (summon != null)
							skill.getEffectedList().add(summon);
					}
				});
		}
		return true;
	}
}
