package com.aionemu.gameserver.model.templates.rewards;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.itemgroups.ItemRaceEntry;

/**
 * @author Rolandas
 */

/**
 * <p>
 * Java class for IdLevelReward complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IdLevelReward">
 *   &lt;complexContent>
 *     &lt;extension base="{}IdReward">
 *       &lt;attribute name="level" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdLevelReward")
public class IdLevelReward extends ItemRaceEntry {

	@XmlAttribute(name = "level", required = true)
	protected int level;

	/**
	 * Gets the value of the level property.
	 */
	public int getLevel() {
		return level;
	}

}
