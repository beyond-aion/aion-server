package com.aionemu.gameserver.model.templates.housing;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "PartType")
@XmlEnum
public enum PartType {
	ROOF(1, 1),
	OUTWALL(2, 2),
	FRAME(3, 3),
	DOOR(4, 4),
	GARDEN(5, 5),
	FENCE(6, 6),
	// 7 is unused
	INWALL_ANY(8, 13),
	INFLOOR_ANY(14, 19),
	// 20 - 26 is unknown, sometimes sent in CM_HOUSE_DECORATE (lineNo)
	ADDON(27, 27);
	// 28 - 29 is unknown, sometimes sent in CM_HOUSE_DECORATE (lineNo)

	private int lineNrStart;
	private int lineNrEnd;

	PartType(int packetLineStart, int packetLineEnd) {
		this.lineNrStart = packetLineStart;
		this.lineNrEnd = packetLineEnd;
	}

	public int getRooms() {
		return lineNrEnd - lineNrStart + 1;
	}

	public int getStartLineNr() {
		return lineNrStart;
	}

	public int getEndLineNr() {
		return lineNrEnd;
	}

	public static PartType getForLineNr(int lineNr) {
		for (PartType type : PartType.values()) {
			if (type.getStartLineNr() <= lineNr && type.getEndLineNr() >= lineNr)
				return type;
		}
		return null;
	}
}
