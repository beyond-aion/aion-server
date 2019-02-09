package com.aionemu.gameserver.model.broker.filter;

import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class BrokerMinMaxFilter extends BrokerFilter {

	private final int min;
	private final int max;

	public BrokerMinMaxFilter(int min, int max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean accept(ItemTemplate template) {
		int templateMask = template.getTemplateId() / 100000;
		return templateMask >= min && templateMask <= max;
	}

}
