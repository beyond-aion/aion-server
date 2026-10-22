package com.aionemu.gameserver.model.templates.item;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TradeinList", propOrder = { "tradeinItem" })
public class TradeinList {

	@XmlElement(name = "tradein_item")
	protected List<TradeinItem> tradeinItem;

	public List<TradeinItem> getTradeinItem() {
		return this.tradeinItem;
	}

}
