package com.aionemu.gameserver.model.ai;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Summons")
public class Summons {

	@XmlElement(name = "percentage")
	private List<Percentage> percentage;

	public List<Percentage> getPercentage() {
		return this.percentage;
	}
}