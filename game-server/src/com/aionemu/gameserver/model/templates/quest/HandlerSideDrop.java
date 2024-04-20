package com.aionemu.gameserver.model.templates.quest;

import com.aionemu.gameserver.dataholders.DataManager;

/**
 * @author vlog, Rolandas
 */
public class HandlerSideDrop extends QuestDrop {

	private int neededAmount;

	public HandlerSideDrop(int questId, int npcId, int itemId, int amount, int chance) {
		this.questId = questId;
		this.npcId = npcId;
		this.itemId = itemId;
		this.chance = chance;

		for (QuestDrop drop : DataManager.QUEST_DATA.getQuestById(questId).getQuestDrop()) {
			if (drop.npcId == npcId && drop.itemId == itemId) {
				this.dropEachMember = drop.dropEachMember;
				break;
			}
		}
		this.neededAmount = amount;
	}

	public HandlerSideDrop(int questId, int npcId, int itemId, int amount, int chance, int step) {
		this(questId, npcId, itemId, amount, chance);
		this.collecting_step = step;
	}

	public int getNeededAmount() {
		return neededAmount;
	}
}
