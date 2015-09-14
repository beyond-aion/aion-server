package com.aionemu.gameserver.model.base;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.base.BaseTemplate;

/**
 * @author Source
 */
public class BaseLocation {

	protected BaseTemplate template;
	protected Race race = Race.NPC;

	public BaseLocation() {
	}

	public BaseLocation(BaseTemplate template) {
		this.template = template;
	}

	public int getId() {
		return template.getId();
	}

	public int getWorldId() {
		return template.getWorldId();
	}

	public Race getRace() {
		return race;
	}

	public void setRace(Race race) {
		this.race = race;
	}

}
