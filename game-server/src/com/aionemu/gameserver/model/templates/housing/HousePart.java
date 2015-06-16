package com.aionemu.gameserver.model.templates.housing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.item.ItemQuality;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "house_part")
public class HousePart {

	@XmlAttribute(name = "building_tags", required = true)
	private List<String> buildingTags;

	@XmlAttribute(required = true)
	protected PartType type;
	
	@XmlAttribute(required = true)
	protected ItemQuality quality;

	@XmlAttribute
	protected String name;

	@XmlAttribute(required = true)
	protected int id;

	@XmlTransient
	protected Set<String> tagsSet = new HashSet<String>(1);

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (buildingTags == null)
			return;

		for (String tag : buildingTags)
			tagsSet.add(tag);

		buildingTags.clear();
		buildingTags = null;
	}
	
	public PartType getType() {
		return type;
	}

	public ItemQuality getQuality() {
		return quality;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}
	
	public Set<String> getTags() {
		return tagsSet;
	}

	public boolean isForBuilding(Building building) {
		return tagsSet.contains(building.getPartsMatchTag());
	}

}
