package com.aionemu.gameserver.model.templates.item;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Acquisition")
public class Acquisition {

	@XmlAttribute(name = "ap", required = false)
	private int ap = 0;

	@XmlAttribute(name = "count", required = false)
	private int itemCount;

	@XmlAttribute(name = "item", required = false)
	private int itemId;

	@XmlAttribute(name = "type", required = true)
	private AcquisitionType type;

	/**
	 * @return the type
	 */
	public AcquisitionType getType() {
		return type;
	}

	/**
	 * @return the itemId
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * @return the itemCount
	 */
	public int getItemCount() {
		return itemCount;
	}

	/**
	 * @return the ap
	 */
	public int getRequiredAp() {
		return ap;
	}

}
