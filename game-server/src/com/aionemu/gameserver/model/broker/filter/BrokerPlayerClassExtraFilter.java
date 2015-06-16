package com.aionemu.gameserver.model.broker.filter;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class BrokerPlayerClassExtraFilter extends BrokerPlayerClassFilter {

	private int mask;

	/**
	 * @param playerClass
	 */
	public BrokerPlayerClassExtraFilter(int mask, PlayerClass playerClass) {
		super(playerClass);
		this.mask = mask;
	}

	@Override
	public boolean accept(ItemTemplate template) {
		return super.accept(template) && mask == template.getTemplateId() / 100000;
	}

}
