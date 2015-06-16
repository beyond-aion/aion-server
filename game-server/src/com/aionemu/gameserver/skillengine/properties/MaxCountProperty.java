package com.aionemu.gameserver.skillengine.properties;

import java.util.SortedMap;
import java.util.TreeMap;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.MathUtil;

/**
 * @author MrPoke
 */
public class MaxCountProperty {

	public static final boolean set(final Skill skill, Properties properties) {
		TargetRangeAttribute value = properties.getTargetType();
		int maxcount = properties.getTargetMaxCount();
		
		switch (value) {
			case AREA:
				int areaCounter = 0;
				final Creature firstTarget = skill.getFirstTarget();
				if (firstTarget == null) {
					return false;
				}
				SortedMap<Double, Creature> sortedMap = new TreeMap<Double, Creature>();
				for (Creature creature : skill.getEffectedList()) {
					sortedMap.put(MathUtil.getDistance(firstTarget, creature), creature);
				}
				skill.getEffectedList().clear();
				for (Creature creature : sortedMap.values()) {
					if (areaCounter >= maxcount && maxcount != 0)
						break;
					skill.getEffectedList().add(creature);
					areaCounter++;
				}
		}
		return true;
	}
}
