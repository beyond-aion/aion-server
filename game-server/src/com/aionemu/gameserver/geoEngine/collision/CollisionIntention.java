package com.aionemu.gameserver.geoEngine.collision;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Rolandas
 */
public enum CollisionIntention {
	NONE(0),
	PHYSICAL(1 << 0), // Physical collision
	MATERIAL(1 << 1), // Mesh materials with skills
	SKILL(1 << 2), // Skill obstacles
	WALK(1 << 3), // Walk/NoWalk obstacles
	DOOR(1 << 4), // Doors which have a state opened/closed
	EVENT(1 << 5), // Appear on event only
	MOVEABLE(1 << 6), // Ships, shugo boxes
	PHYSICAL_SEE_THROUGH(1 << 7),
	DEFAULT_COLLISIONS(PHYSICAL.getId() | DOOR.getId() | PHYSICAL_SEE_THROUGH.getId()),
	CANT_SEE_COLLISIONS(PHYSICAL.getId() | DOOR.getId()),
	// This is used for nodes only, means they allow to enumerate their child geometries
	// Nodes which do not specify it won't let their children enumerated for collisions,
	// to speed up processing
	ALL(PHYSICAL.getId() | MATERIAL.getId() | SKILL.getId() | WALK.getId() | DOOR.getId()
			| EVENT.getId() | MOVEABLE.getId() | PHYSICAL_SEE_THROUGH.getId());

	private byte id;

	private CollisionIntention(int id) {
		this.id = (byte) id;
	}

	public byte getId() {
		return id;
	}

	public static Set<CollisionIntention> getFlagsFromValue(int value) {
		EnumSet<CollisionIntention> result = EnumSet.noneOf(CollisionIntention.class);
		for (CollisionIntention m : CollisionIntention.values()) {
			if ((value & m.getId()) == m.getId()) {
				if (m == NONE || m == ALL)
					continue;
				result.add(m);
			}
		}
		return result;
	}

	public static String toString(int value) {
		String str = "";
		for (CollisionIntention m : CollisionIntention.values()) {
			if (m == NONE || m == ALL)
				continue;
			if ((value & m.getId()) == m.getId()) {
				str += m.toString();
				str += ", ";
			}
		}
		if (str.length() > 0)
			str = str.substring(0, str.length() - 2);
		return str;
	}
}
