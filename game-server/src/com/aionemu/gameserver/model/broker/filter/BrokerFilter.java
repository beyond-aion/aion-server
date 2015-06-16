package com.aionemu.gameserver.model.broker.filter;

import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public abstract class BrokerFilter {

	/**
	 * @param template
	 * @return
	 */
	public abstract boolean accept(ItemTemplate template);
}
