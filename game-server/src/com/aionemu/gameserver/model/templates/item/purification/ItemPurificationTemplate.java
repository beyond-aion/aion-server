package com.aionemu.gameserver.model.templates.item.purification;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.stats.calc.StatOwner;

/**
 * @author Ranastic
 * @reworked Navyan
 */
@XmlRootElement(name = "ItemPurification")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemPurificationTemplate implements StatOwner {

	@XmlElement(name = "purification_result_item", required = true)
	private List<PurificationResultItem> purificationResultItems;
	@XmlAttribute(name = "base_item")
	private int baseItemId;

	public List<PurificationResultItem> getPurificationResultItems() {
		return purificationResultItems;
	}

	public int getBaseItemId() {
		return baseItemId;
	}
}
