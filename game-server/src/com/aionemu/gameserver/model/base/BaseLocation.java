package com.aionemu.gameserver.model.base;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.base.BaseTemplate;

/**
 * @author Source
 */
public class BaseLocation {

	protected BaseTemplate template;
	protected BaseType type;
	protected Race race;

	public BaseLocation(BaseTemplate template) {
		this.template = template;
		this.type = template.getType();
		this.race = Race.NPC;
	}

	public int getId() {
		return template.getId();
	}

	public int getWorldId() {
		return template.getWorldId();
	}
	
	public BaseType getType() {
		return type;
	}

	public Race getRace() {
		return race;
	}

	public void setRace(Race race) {
		this.race = race;
	}

}
