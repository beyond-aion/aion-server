package com.aionemu.gameserver.model.base;

import com.aionemu.gameserver.model.templates.base.BaseTemplate;

/**
 * @author Estrayl
 *
 */
public class StainedBaseLocation extends BaseLocation {
	
	private final BaseColorType color;

	public StainedBaseLocation(BaseTemplate template) {
		super(template);
		this.color = template.getColor();
	}

	public BaseColorType getColor() {
		return color;
	}
}
