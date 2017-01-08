package com.aionemu.gameserver.model.templates.recipe;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComboProduct")
public class ComboProduct {

	@XmlAttribute
	protected int itemid;

	/**
	 * @return the itemid
	 */
	public int getItemId() {
		return itemid;
	}

}
