package com.aionemu.gameserver.model.templates.item;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssemblyItem")
public class AssemblyItem {

	@XmlAttribute(required = true)
	protected List<Integer> parts;
	@XmlAttribute(required = true)
	protected int id;

	public List<Integer> getParts() {
		if (parts == null) {
			parts = new ArrayList<>();
		}
		return this.parts;
	}

	/**
	 * Gets the value of the id property.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the value of the id property.
	 */
	public void setId(int value) {
		this.id = value;
	}

}
