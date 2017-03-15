package com.aionemu.gameserver.model.base;

import com.aionemu.gameserver.model.templates.base.BaseTemplate;

/**
 * @author Estrayl
 *
 */
public class StainedBaseLocation extends BaseLocation {
	
	private BaseColorType color;
	private boolean isEnhanced;

	public StainedBaseLocation(BaseTemplate template) {
		super(template);
		this.color = template.getColor();
		this.isEnhanced = false;
	}

	public BaseColorType getColor() {
		return color;
	}
	
	public boolean isEnhanced() {
		return isEnhanced;
	}
	
	public void setEnhanced(boolean isEnhanced) {
		this.isEnhanced = isEnhanced;
	}
}
