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
@XmlType(name = "tempering_list")
@XmlAccessorType(XmlAccessType.FIELD)
public class TemperingList {

	@XmlElement(name = "tempering_data", required = true)
	protected List<TemperingTemplateData> temperingDatas;

	@XmlAttribute(name = "item_group", required = true)
	private String itemGroup;

	public String getItemGroup() {
		return itemGroup;
	}

	public List<TemperingTemplateData> getTemperingDatas() {
		return temperingDatas;
	}

}
