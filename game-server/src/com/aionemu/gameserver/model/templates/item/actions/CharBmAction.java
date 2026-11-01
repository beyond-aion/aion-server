package com.aionemu.gameserver.model.templates.item.actions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Grants a timed character benefit, for example more house interior object slots (HousingObj_Interior_expansion, value = slots).
 * <p>
 * TODO logic: needs a per-character benefit list with expiry and a house object limit that can be raised.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CharBmAction")
public class CharBmAction extends UnimplementedItemAction {

	@XmlAttribute
	private String name;
	@XmlAttribute
	private int minutes;
	@XmlAttribute
	private int value;

	public String getName() {
		return name;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getValue() {
		return value;
	}
}
