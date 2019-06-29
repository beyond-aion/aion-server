package com.aionemu.gameserver.model.templates.housing;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.item.ItemQuality;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "house_part")
public class HousePart {

	@XmlAttribute(required = true)
	private int id;

	@XmlAttribute
	private String name;

	@XmlAttribute(required = true)
	private ItemQuality quality;

	@XmlAttribute(required = true)
	private PartType type;

	@XmlAttribute(name = "building_tags", required = true)
	private Set<String> buildingTags;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ItemQuality getQuality() {
		return quality;
	}

	public PartType getType() {
		return type;
	}

	public Set<String> getTags() {
		return buildingTags;
	}

	public boolean isForBuilding(Building building) {
		return buildingTags.contains(building.getPartsMatchTag());
	}

}
