package com.aionemu.gameserver.model.base;

import com.aionemu.gameserver.model.templates.base.BaseTemplate;

/**
 * @author Estrayl
 */
public class PanesterraBaseLocation extends BaseLocation {

	public PanesterraBaseLocation(BaseTemplate template) {
		super(template);
		if (template.getType() == BaseType.PANESTERRA_FACTION_CAMP)
			occupier = BaseOccupier.PEACE;
	}
}
