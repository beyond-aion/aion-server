package com.aionemu.gameserver.model.templates.quest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestDrop")
public class QuestDrop {

	@XmlAttribute(name = "npc_id")
	protected Integer npcId;
	@XmlAttribute(name = "item_id")
	protected Integer itemId;
	@XmlAttribute
	protected Integer chance;
	@XmlAttribute(name = "drop_each_member")
	protected int dropEachMember = 0;
	@XmlAttribute(name = "collecting_step")
	protected int collecting_step = 0;

	@XmlTransient
	protected Integer questId;

	/**
	 * Gets the value of the npcId property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getNpcId() {
		return npcId;
	}

	/**
	 * Gets the value of the itemId property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getItemId() {
		return itemId;
	}

	/**
	 * Gets the value of the chance property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public int getChance() {
		if (chance == null)
			return 100;
		return chance;
	}

	public boolean isDropEachMemberGroup() {
		return dropEachMember == 1;
	}

	public boolean isDropEachMemberAlliance() {
		return dropEachMember == 1 || dropEachMember == 2;
	}

	/**
	 * @return the questId
	 */
	public Integer getQuestId() {
		return questId;
	}

	public int getCollectingStep() {
		return collecting_step;
	}

	/**
	 * @param questId
	 *          the questId to set
	 */
	public void setQuestId(Integer questId) {
		this.questId = questId;
	}

}
