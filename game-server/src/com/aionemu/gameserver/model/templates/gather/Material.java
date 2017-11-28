package com.aionemu.gameserver.model.templates.gather;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.L10n;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Material")
public class Material implements Comparable<Material>, L10n {

	@XmlAttribute
	protected String name;
	@XmlAttribute
	protected int itemid;
	@XmlAttribute
	protected int nameid;
	@XmlAttribute
	protected int rate;

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the itemid
	 */
	public int getItemId() {
		return itemid;
	}

	/**
	 * Gets the value of the nameid property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	@Override
	public int getL10nId() {
		return nameid;
	}

	/**
	 * Gets the value of the rate property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public int getRate() {
		return rate;
	}

	@Override
	public int compareTo(Material o) {
		return o.rate - rate;
	}

}
