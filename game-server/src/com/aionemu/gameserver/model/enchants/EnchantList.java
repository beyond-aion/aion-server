package com.aionemu.gameserver.model.enchants;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlType(name = "enchant_list")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnchantList {

	@XmlElement(name = "enchant_data", required = true)
	protected List<EnchantTemplateData> enchantDatas;

	@XmlAttribute(name = "item_group", required = true)
	private String itemGroup;

	public List<EnchantTemplateData> getEnchantDatas() {
		return enchantDatas;
	}

	public String getItemGroup() {
		return itemGroup;
	}
}
