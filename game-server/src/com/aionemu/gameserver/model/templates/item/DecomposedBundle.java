package com.aionemu.gameserver.model.templates.item;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "DecomposedBundle")
public class DecomposedBundle implements DecomposedReward {

	@XmlAttribute(name = "chance")
	private float chance = 100;
	@XmlElement(name = "item")
	private List<DecomposedItem> items;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (chance <= 0 || chance > 100)
			throw new IllegalArgumentException("Decomposable reward bundle chance (" + chance + ") must be within (0, 100]");
	}

	@Override
	public float getChance() {
		return chance;
	}

	@Override
	public List<DecomposedItem> getItems() {
		return items;
	}
}
