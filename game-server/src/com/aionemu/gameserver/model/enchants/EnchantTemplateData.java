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
@XmlType(name = "enchant_data")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnchantTemplateData {

	@XmlElement(name = "enchant_stat", required = true)
	protected List<EnchantStat> enchantStats;

	@XmlAttribute(name = "level", required = true)
	private int level;

	public List<EnchantStat> getEnchantStats() {
		return enchantStats;
	}

	public int getLevel() {
		return level;
	}

}
