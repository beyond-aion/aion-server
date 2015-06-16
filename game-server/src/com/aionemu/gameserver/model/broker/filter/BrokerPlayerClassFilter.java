package com.aionemu.gameserver.model.broker.filter;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class BrokerPlayerClassFilter extends BrokerFilter {

	private PlayerClass playerClass;

	/**
	 * @param playerClass
	 */
	public BrokerPlayerClassFilter(PlayerClass playerClass) {
		super();
		this.playerClass = playerClass;
	}

	@Override
	public boolean accept(ItemTemplate template) {
		return template.isClassSpecific(playerClass);
	}

}
