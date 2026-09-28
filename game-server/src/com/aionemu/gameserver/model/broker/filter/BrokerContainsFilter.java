package com.aionemu.gameserver.model.broker.filter;

import java.util.Arrays;

import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class BrokerContainsFilter extends BrokerFilter {

	private final int[] masks;

	public BrokerContainsFilter(int... masks) {
		this.masks = masks;
	}

	@Override
	public boolean accept(ItemTemplate template) {
		int mask = template.getTemplateId() / 100000;
		return Arrays.stream(masks).anyMatch(i -> i == mask);
	}

}
