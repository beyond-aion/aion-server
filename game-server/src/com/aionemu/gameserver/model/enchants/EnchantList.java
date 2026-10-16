package com.aionemu.gameserver.model.enchants;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

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
