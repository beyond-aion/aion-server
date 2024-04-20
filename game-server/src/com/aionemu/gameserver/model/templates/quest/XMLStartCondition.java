package com.aionemu.gameserver.model.templates.quest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.QuestStateList;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Checks quest start conditions, listed in quest_data.xml
 * 
 * @author antness, vlog
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestStartConditions")
public class XMLStartCondition {

	@XmlElement(name = "finished")
	protected List<FinishedQuestCond> finished;
	@XmlList
	@XmlElement(name = "unfinished", type = Integer.class)
	protected List<Integer> unfinished;
	@XmlList
	@XmlElement(name = "noacquired", type = Integer.class)
	protected List<Integer> noacquired;
	@XmlList
	@XmlElement(name = "acquired", type = Integer.class)
	protected List<Integer> acquired;
	@XmlList
	@XmlElement(name = "equipped", type = Integer.class)
	protected List<Integer> equipped;
	@XmlElement(name = "required_title")
	protected int requiredTitle;

	public boolean isOptional() {
		return finished != null && finished.size() > 0;
	}

	/** Check, if the player has finished listed quests */
	private boolean checkFinishedQuests(QuestStateList qsl) {
		if (finished != null && finished.size() > 0) {
			for (FinishedQuestCond fqc : finished) {
				int questId = fqc.getQuestId();
				int reward = fqc.getReward();
				QuestState qs = qsl.getQuestState(questId);
				if (qs == null || qs.getStatus() != QuestStatus.COMPLETE || (reward >= 0 && (qs.getRewardGroup() == null || reward != qs.getRewardGroup()))) {
					return false;
				}
				QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
				if (template != null && template.isRepeatable()) {
					if (template.getMaxRepeatCount() != 255 && qs.getCompleteCount() != template.getMaxRepeatCount()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/** Check, if the player has not finished listed quests */
	private boolean checkUnfinishedQuests(QuestStateList qsl) {
		if (unfinished != null && unfinished.size() > 0) {
			for (Integer questId : unfinished) {
				QuestState qs = qsl.getQuestState(questId);
				if (qs != null && qs.getStatus() == QuestStatus.COMPLETE)
					return false;
			}
		}
		return true;
	}

	/** Check, if the player has not acquired listed quests */
	private boolean checkNoAcquiredQuests(QuestStateList qsl) {
		if (noacquired != null && noacquired.size() > 0) {
			for (Integer questId : noacquired) {
				QuestState qs = qsl.getQuestState(questId);
				if (qs != null && (qs.getStatus() == QuestStatus.START || qs.getStatus() == QuestStatus.REWARD))
					return false;
			}
		}
		return true;
	}

	/** Check, if the player has acquired listed quests */
	private boolean checkAcquiredQuests(QuestStateList qsl) {
		if (acquired != null && acquired.size() > 0) {
			for (Integer questId : acquired) {
				QuestState qs = qsl.getQuestState(questId);
				if (qs == null || qs.getStatus() == QuestStatus.LOCKED)
					return false;
			}
		}
		return true;
	}

	private boolean checkEquippedItems(Player player, boolean warn) {
		if (!warn)
			return true;
		if (equipped != null && equipped.size() > 0) {
			for (int itemId : equipped) {
				if (!player.getEquipment().getEquippedItemIds().contains(itemId)) {
					String requiredItem = DataManager.ITEM_DATA.getItemTemplate(itemId).getL10n();
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_EQUIP_ITEM(requiredItem));
					return false;
				}
			}
		}
		return true;
	}

	private boolean isRequiredTitleDisplayed(Player player) {
		if (requiredTitle != 0 && player.getCommonData().getTitleId() != requiredTitle)
			return false;
		return true;
	}

	/** Check all conditions */
	public boolean check(Player player, boolean warn) {
		QuestStateList qsl = player.getQuestStateList();
		return checkFinishedQuests(qsl) && checkUnfinishedQuests(qsl) && checkAcquiredQuests(qsl) && checkNoAcquiredQuests(qsl)
			&& checkEquippedItems(player, warn) && isRequiredTitleDisplayed(player);
	}

	public List<FinishedQuestCond> getFinishedPreconditions() {
		return finished;
	}
}
