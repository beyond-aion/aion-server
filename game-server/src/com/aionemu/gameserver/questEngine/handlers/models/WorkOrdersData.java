package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.WorkOrders;

/**
 * @author Mr. Poke, reworked Bobobear
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorkOrdersData", propOrder = { "giveComponent" })
public class WorkOrdersData extends XMLQuest {

	@XmlElement(name = "give_component", required = true)
	protected List<QuestItems> giveComponent;
	@XmlAttribute(name = "start_npc_ids", required = true)
	protected List<Integer> startNpcIds;
	@XmlAttribute(name = "recipe_id", required = true)
	protected int recipeId;

	/**
	 * Gets the value of the giveComponent property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the giveComponent property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getGiveComponent().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QuestItems }
	 */
	public List<QuestItems> getGiveComponent() {
		if (giveComponent == null) {
			giveComponent = new FastTable<QuestItems>();
		}
		return this.giveComponent;
	}

	/**
	 * Gets the value of the startNpcIds property.
	 */
	public List<Integer> getStartNpcIds() {
		return startNpcIds;
	}

	/**
	 * Gets the value of the recipeId property.
	 */
	public int getRecipeId() {
		return recipeId;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.gameserver.questEngine.handlers.models.QuestScriptData#register()
	 */
	@Override
	public void register(QuestEngine questEngine) {
		questEngine.addQuestHandler(new WorkOrders(this));
	}
}
