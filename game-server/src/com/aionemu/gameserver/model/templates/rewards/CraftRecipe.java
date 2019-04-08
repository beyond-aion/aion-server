package com.aionemu.gameserver.model.templates.rewards;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.QuestTemplate;

/**
 * <p>
 * Java class for CraftRecipe complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CraftRecipe">
 *   &lt;complexContent>
 *     &lt;extension base="{}CraftReward">
 *       &lt;attribute name="level" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CraftRecipe")
public class CraftRecipe extends CraftReward {

	@XmlAttribute(name = "level", required = true)
	private int level;

	/**
	 * Gets the value of the level property.
	 */
	public int getLevel() {
		return level;
	}

	@Override
	protected boolean matchesQuest(QuestTemplate questTemplate) {
		if (!super.matchesQuest(questTemplate))
			return false;
		if (questTemplate.getCombineSkillPoint() < level)
			return false;
		if (questTemplate.getCombineSkillPoint() > getMaxLevel())
			return false;
		return true;
	}

	private int getMaxLevel() {
		return Math.min(level + 40, level / 100 * 100 + 99);
	}
}
