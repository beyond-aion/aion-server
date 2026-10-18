package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.housing.HousePart;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "houseParts" })
@XmlRootElement(name = "house_parts")
public class HousePartsData {

	@XmlElement(name = "house_part")
	private List<HousePart> houseParts;

	@XmlTransient
	private Map<Integer, HousePart> partsById = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (houseParts == null)
			return;

		for (HousePart part : houseParts)
			partsById.put(part.getId(), part);
		houseParts = null;
	}

	public HousePart getPartById(int partId) {
		return partsById.get(partId);
	}

	public int size() {
		return partsById.size();
	}

}
