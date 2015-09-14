package com.aionemu.gameserver.world.zone;

import java.util.List;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "ZoneAttributes")
@XmlEnum(String.class)
public enum ZoneAttributes {
	BIND(1 << 0),
	RECALL(1 << 1),
	GLIDE(1 << 2),
	FLY(1 << 3),
	RIDE(1 << 4),
	FLY_RIDE(1 << 5),

	@XmlEnumValue("PVP")
	PVP_ENABLED(1 << 6), // Only for PvP type zones
	@XmlEnumValue("DUEL_SAME_RACE")
	DUEL_SAME_RACE_ENABLED(1 << 7), // Only for Duel type zones
	@XmlEnumValue("DUEL_OTHER_RACE")
	DUEL_OTHER_RACE_ENABLED(1 << 8), // Only for Duel type zones

	NO_RETURN_BATTLE(1 << 9); // in client XML it's no_return_battlefield attribute

	private int id;

	private ZoneAttributes(int id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	public static Integer fromList(List<ZoneAttributes> flagValues) {
		Integer result = 0;
		for (ZoneAttributes attribute : ZoneAttributes.values()) {
			if (flagValues.contains(attribute))
				result |= attribute.getId();
		}
		return result;
	}
}
