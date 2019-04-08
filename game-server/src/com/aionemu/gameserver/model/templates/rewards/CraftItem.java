package com.aionemu.gameserver.model.templates.rewards;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.templates.QuestTemplate;

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
 * 
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CraftItem")
public class CraftItem extends CraftReward {

	@XmlAttribute(name = "minLevel", required = true)
	private int minLevel;

	@XmlAttribute(name = "maxLevel", required = true)
	private int maxLevel;

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

	@Override
	public long getCount() {
		return Rnd.get(3, 5);
	}

	@Override
	protected boolean matchesQuest(QuestTemplate questTemplate) {
		if (!super.matchesQuest(questTemplate))
			return false;
		if (questTemplate.getCombineSkillPoint() < minLevel)
			return false;
		if (questTemplate.getCombineSkillPoint() > maxLevel)
			return false;
		return true;
	}
}
