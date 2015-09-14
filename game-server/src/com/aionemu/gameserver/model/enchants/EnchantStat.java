package com.aionemu.gameserver.model.enchants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author xTz
 */
@XmlType(name = "enchant_stat")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnchantStat {

	@XmlAttribute(required = true)
	protected StatEnum stat;
	@XmlAttribute(required = true)
	protected int value;

	public StatEnum getStat() {
		return stat;
	}

	public int getValue() {
		return value;
	}

}
