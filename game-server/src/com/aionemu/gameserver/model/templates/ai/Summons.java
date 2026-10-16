package com.aionemu.gameserver.model.templates.ai;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Summons")
public class Summons {

	@XmlElement(name = "percentage")
	private List<Percentage> percentage;

	public List<Percentage> getPercentage() {
		return percentage;
	}
}
