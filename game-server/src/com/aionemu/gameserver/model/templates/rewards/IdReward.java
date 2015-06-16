package com.aionemu.gameserver.model.templates.rewards;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author Rolandas
 */

/**
 * <p>
 * Java class for IdReward complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IdReward">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="race" type="{}Race" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdReward")
@XmlSeeAlso({ IdLevelReward.class })
public class IdReward {

	@XmlAttribute(name = "id", required = true)
	protected int id;

	@XmlAttribute(name = "race")
	protected Race race;

	/**
	 * Gets the value of the id property.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the value of the race property.
	 * 
	 * @return possible object is {@link Race }
	 */
	public Race getRace() {
		return race;
	}

	/**
	 * Method is used to check item race; Some items having PC_ALL really are not for both races, like some foods and
	 * weapons
	 * 
	 * @param playerRace
	 *          player's race
	 * @return true if race is correct for player when overridden or not from templates
	 */
	public boolean checkRace(Race playerRace) {
		ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(id);
		return template.getRace() == Race.PC_ALL && (race == null || race == playerRace) || template.getRace() != Race.PC_ALL
			&& template.getRace() == playerRace;
	}

}
