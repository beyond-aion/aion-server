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
