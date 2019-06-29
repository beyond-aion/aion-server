package com.aionemu.gameserver.model.templates.housing;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.dataholders.DataManager;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "building")
public class Building {

	@XmlAttribute(required = true)
	private int id;

	@XmlElement(name = "parts")
	private Parts parts;

	@XmlAttribute(name = "default")
	private boolean isDefault;

	@XmlAttribute(name = "parts_match")
	private String partsMatch;

	@XmlAttribute
	private HouseType size;

	@XmlAttribute
	private BuildingType type;

	@XmlTransient
	private Map<PartType, Integer> partsByType;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (parts == null)
			return;
		partsByType = new EnumMap<>(PartType.class);
		if (parts.getDoor() != 0)
			partsByType.put(PartType.DOOR, parts.getDoor());
		if (parts.getFence() != null)
			partsByType.put(PartType.FENCE, parts.getFence());
		if (parts.getFrame() != null)
			partsByType.put(PartType.FRAME, parts.getFrame());
		if (parts.getGarden() != null)
			partsByType.put(PartType.GARDEN, parts.getGarden());
		if (parts.getInfloor() != 0)
			partsByType.put(PartType.INFLOOR_ANY, parts.getInfloor());
		if (parts.getInwall() != 0)
			partsByType.put(PartType.INWALL_ANY, parts.getInwall());
		if (parts.getOutwall() != null)
			partsByType.put(PartType.OUTWALL, parts.getOutwall());
		if (parts.getRoof() != null)
			partsByType.put(PartType.ROOF, parts.getRoof());
	}

	public int getId() {
		return id;
	}

	public boolean isDefault() {
		return isDefault;
	}

	// All DataManager calls are just to ensure integrity if called from housing land templates. Because buildings in land templates have only id and
	// isDefault set. Buildings template has full info though, except isDefault value for the land.

	public String getPartsMatchTag() {
		if (partsMatch == null)
			return DataManager.HOUSE_BUILDING_DATA.getBuilding(id).partsMatch;
		return partsMatch;
	}

	public HouseType getSize() {
		if (size == null)
			return DataManager.HOUSE_BUILDING_DATA.getBuilding(id).size;
		return size;
	}

	public BuildingType getType() {
		if (type == null)
			return DataManager.HOUSE_BUILDING_DATA.getBuilding(id).type;
		return type;
	}

	public Integer getDefaultDecorId(PartType partType) {
		return getPartsByType().get(partType);
	}

	public List<Integer> getDefaultPartIds() {
		return new ArrayList<>(getPartsByType().values());
	}

	private Map<PartType, Integer> getPartsByType() {
		if (partsByType == null)
			return DataManager.HOUSE_BUILDING_DATA.getBuilding(id).partsByType;
		return partsByType;
	}
}
