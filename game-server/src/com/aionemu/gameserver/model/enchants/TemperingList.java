package com.aionemu.gameserver.model.enchants;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;

/**
 * @author xTz
 */
@XmlType(name = "tempering_list")
@XmlAccessorType(XmlAccessType.FIELD)
public class TemperingList {

	@XmlElement(name = "tempering_data", required = true)
	protected List<TemperingTemplateData> temperingDatas;

	@XmlAttribute(name = "item_group", required = true)
	private ItemGroup itemGroup = ItemGroup.NONE;

	public ItemGroup getItemGroup() {
		return itemGroup;
	}

	public List<TemperingTemplateData> getTemperingDatas() {
		return temperingDatas;
	}

}
