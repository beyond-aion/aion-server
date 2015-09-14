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
@XmlType(name = "tempering_data")
@XmlAccessorType(XmlAccessType.FIELD)
public class TemperingTemplateData {

	@XmlElement(name = "tempering_stat", required = true)
	protected List<TemperingStat> temperingStats;

	@XmlAttribute(name = "level", required = true)
	private int level;

	public int getLevel() {
		return level;
	}

	public List<TemperingStat> getTemperingStats() {
		return temperingStats;
	}

}
