package com.aionemu.gameserver.model.base;

import com.aionemu.gameserver.model.templates.base.BaseTemplate;

/**
 * @author Source
 */
public class BaseLocation {

	protected BaseTemplate template;
	protected BaseType type;
	protected BaseOccupier occupier;

	public BaseLocation(BaseTemplate template) {
		this.template = template;
		this.type = template.getType();
		this.occupier = template.getDefaultOccupier();
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

	public BaseOccupier getOccupier() {
		return occupier;
	}

	public void setOccupier(BaseOccupier occupier) {
		this.occupier = occupier;
	}

	public BaseTemplate getTemplate() {
		return template;
	}
}
