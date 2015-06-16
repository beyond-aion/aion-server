package com.aionemu.gameserver.model.broker.filter;

import org.apache.commons.lang3.ArrayUtils;

import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class BrokerContainsFilter extends BrokerFilter {

	private int[] masks;

	/**
	 * @param masks
	 */
	public BrokerContainsFilter(int... masks) {
		this.masks = masks;
	}

	@Override
	public boolean accept(ItemTemplate template) {
		return ArrayUtils.contains(masks, template.getTemplateId() / 100000);
	}

}
