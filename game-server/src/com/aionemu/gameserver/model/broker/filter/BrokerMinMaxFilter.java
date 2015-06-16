package com.aionemu.gameserver.model.broker.filter;

import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class BrokerMinMaxFilter extends BrokerFilter {

	private int min;
	private int max;

	/**
	 * @param min
	 * @param max
	 */
	public BrokerMinMaxFilter(int min, int max) {
		this.min = min * 100000;
		this.max = max * 100000;
	}

	@Override
	public boolean accept(ItemTemplate template) {
		return template.getTemplateId() >= min && template.getTemplateId() < max;
	}

}
