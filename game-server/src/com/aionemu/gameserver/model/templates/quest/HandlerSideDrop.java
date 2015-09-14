package com.aionemu.gameserver.model.templates.quest;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.QuestTemplate;

/**
 * @author vlog
 * @modified Rolandas
 */
public class HandlerSideDrop extends QuestDrop {

	private int neededAmount;

	public HandlerSideDrop(int questId, int npcId, int itemId, int amount, int chance) {
		this.questId = questId;
		this.npcId = npcId;
		this.itemId = itemId;
		this.chance = chance;

		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		for (QuestDrop drop : template.getQuestDrop()) {
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
