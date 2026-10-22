package com.aionemu.gameserver.model.broker.filter;

import java.util.Arrays;

import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class BrokerContainsExtraFilter extends BrokerFilter {

	private final int[] masks;

	public BrokerContainsExtraFilter(int... masks) {
		this.masks = masks;
	}

	@Override
	public boolean accept(ItemTemplate template) {
		int mask = template.getTemplateId() / 10000;
		return Arrays.stream(masks).anyMatch(i -> i == mask);
	}

}
