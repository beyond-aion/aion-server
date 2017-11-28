package com.aionemu.gameserver.model;

import javax.xml.bind.annotation.XmlEnum;

import com.aionemu.gameserver.model.templates.L10n;

/**
 * Basic enum with races.<br>
 * I believe that NPCs will have their own races, so it's quite comfortable to have it in the same place
 * 
 * @author SoulKeeper
 */
@XmlEnum
public enum Race implements L10n {
	/**
	 * Playable races
	 */
	ELYOS(0, 900240),
	ASMODIANS(1, 900241),

	/**
	 * Npc races
	 */
	LYCAN(2),
	CONSTRUCT(3),
	CARRIER(4),
	DRAKAN(5),
	LIZARDMAN(6),
	TELEPORTER(7),
	NAGA(8),
	BROWNIE(9),
	KRALL(10),
	SHULACK(11),
	BARRIER(12),
	PC_LIGHT_CASTLE_DOOR(13),
	PC_DARK_CASTLE_DOOR(14),
	DRAGON_CASTLE_DOOR(15),
	GCHIEF_LIGHT(16),
	GCHIEF_DARK(17),
	DRAGON(18),
	OUTSIDER(19),
	RATMAN(20),
	DEMIHUMANOID(21),
	UNDEAD(22),
	BEAST(23),
	MAGICALMONSTER(24),
	ELEMENTAL(25),
	LIVINGWATER(28),

	/**
	 * Special races
	 */
	NONE(26),
	PC_ALL(27),
	DEFORM(28),

	// 2.6
	NEUT(29),
	// 2.7 -- NOT SURE !!!
	GHENCHMAN_LIGHT(30),
	GHENCHMAN_DARK(31),
	// 3.0
	EVENT_TOWER_DARK(32),
	EVENT_TOWER_LIGHT(33),
	GOBLIN(34),
	TRICODARK(35),
	NPC(36),
	// 3.5
	LIGHT(37),
	DARK(38),
	WORLD_EVENT_DEFTOWER(39),
	// 4.3
	ORC(40),
	DRAGONET(41),
	SIEGEDRAKAN(42),
	GCHIEF_DRAGON(43),
	WORLD_EVENT_BONFIRE(44),
	// 4.7.5
	DOOR_KILLER(45),
	// 4.8.0
	LF5_Q_ITEM(46);

	private int raceId;
	private int l10nId;

	/**
	 * Constructors
	 */
	private Race(int raceId) {
		this(raceId, 0);
	}

	private Race(int raceId, int l10nId) {
		this.raceId = raceId;
		this.l10nId = l10nId;
	}

	/**
	 * Accessors
	 */
	public int getRaceId() {
		return raceId;
	}

	public boolean isAsmoOrEly() {
		return this == ELYOS || this == ASMODIANS;
	}

	public boolean isPlayerRace() {
		return isAsmoOrEly() || this == PC_ALL;
	}

	@Override
	public int getL10nId() {
		return l10nId;
	}

	public static Race getRaceByString(String fieldName) {
		for (Race r : values()) {
			if (r.toString().equals(fieldName))
				return r;
		}
		return null;
	}
}
