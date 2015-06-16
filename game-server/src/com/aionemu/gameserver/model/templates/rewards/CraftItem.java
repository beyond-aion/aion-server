package com.aionemu.gameserver.model.templates.rewards;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 *
 */

/**
 * <p>
 * Java class for CraftItem complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CraftItem">
 *   &lt;complexContent>
 *     &lt;extension base="{}CraftReward">
 *       &lt;attribute name="minLevel" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="maxLevel" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CraftItem")
public class CraftItem extends CraftReward {

	@XmlAttribute(name = "minLevel", required = true)
	protected int minLevel;

	@XmlAttribute(name = "maxLevel", required = true)
	protected int maxLevel;

	/**
	 * Gets the value of the minLevel property.
	 */
	public int getMinLevel() {
		return minLevel;
	}

	/**
	 * Gets the value of the maxLevel property.
	 */
	public int getMaxLevel() {
		return maxLevel;
	}

}
