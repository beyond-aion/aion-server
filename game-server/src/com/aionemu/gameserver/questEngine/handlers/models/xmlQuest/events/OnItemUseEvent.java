package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * Runs its operations when the player uses one of the listed quest items.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OnItemUseEvent")
public class OnItemUseEvent extends QuestEvent {

	@XmlAttribute(name = "item_ids", required = true)
	protected List<Integer> itemIds;

	public List<Integer> getItemIds() {
		if (itemIds == null)
			itemIds = new ArrayList<>();
		return itemIds;
	}

	public boolean operate(QuestEnv env, int itemId) {
		if (itemIds == null || !itemIds.contains(itemId))
			return false;
		if (conditions != null && !conditions.checkConditionOfSet(env))
			return false;
		return operations != null && operations.operate(env);
	}
}
