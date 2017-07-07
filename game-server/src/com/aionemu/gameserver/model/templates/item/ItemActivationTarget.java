package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlEnum;

import com.aionemu.gameserver.model.Race;

/**
 * @author Rolandas
 */
@XmlEnum
public enum ItemActivationTarget {

	STANDALONE,
	TARGET,
	MYMENTO,
	WORLD_EVENT_CAKE_D,
	WORLD_EVENT_CAKE_L,

	// races
	BROWNIE(Race.BROWNIE),
	GCHIEF_LIGHT(Race.GCHIEF_LIGHT),
	GHENCHMAN_LIGHT(Race.GHENCHMAN_LIGHT),
	GHENCHMAN_DARK(Race.GHENCHMAN_DARK),
	KRALL(Race.KRALL),
	LF5_Q_ITEM(Race.LF5_Q_ITEM),
	LIVINGWATER(Race.LIVINGWATER),
	LYCAN(Race.LYCAN),
	EVENT_TOWER_LIGHT(Race.EVENT_TOWER_LIGHT),
	EVENT_TOWER_DARK(Race.EVENT_TOWER_DARK),
	WORLD_EVENT_DEFTOWER(Race.WORLD_EVENT_DEFTOWER);

	private final Race race;

	private ItemActivationTarget() {
		this(null);
	}

	private ItemActivationTarget(Race race) {
		this.race = race;
	}

	public Race getRace() {
		return race;
	}

}
