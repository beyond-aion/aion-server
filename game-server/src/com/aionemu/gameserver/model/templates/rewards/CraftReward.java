package com.aionemu.gameserver.model.templates.rewards;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.itemgroups.ItemRaceEntry;

/**
 * @author Rolandas
 *
 */

/**
 * <p>
 * Java class for CraftReward complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CraftReward">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="skill" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="race" type="{}Race" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CraftReward")
@XmlSeeAlso({ CraftRecipe.class, CraftItem.class })
public abstract class CraftReward extends ItemRaceEntry {

	@XmlAttribute(name = "skill")
	protected Integer skill;

	/**
	 * Gets the value of the skill property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getSkill() {
		return skill;
	}

}
