package com.aionemu.gameserver.services.reward;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dao.RewardServiceDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.model.templates.rewards.RewardEntryItem;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION.ActionType;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author KID, Neon
 */
public class WebRewardService {

	private static final Logger log = LoggerFactory.getLogger("WEB_REWARDS_LOG");
	private static WebRewardService instance = new WebRewardService();

	public static WebRewardService getInstance() {
		return instance;
	}

	private WebRewardService() {
	}

	public void sendAvailableRewards(Player player) {
		if (player == null)
			return;
		List<RewardEntryItem> list = RewardServiceDAO.loadUnreceived(player.getObjectId());
		if (list.size() == 0)
			return;

		List<Integer> rewarded = new ArrayList<>();
		for (RewardEntryItem item : list) {
			try {
				if (sendRewardItem(player, item) || executeRewardAction(player, item)) {
					log.info("[WebRewardService][" + item.getEntryId() + "] " + player + " has received " + item);
					rewarded.add(item.getEntryId());
				} else {
					log.warn("[WebRewardService][" + item.getEntryId() + "] " + player + " could not receive " + item);
				}
			} catch (Exception e) {
				log.error("[WebRewardService][" + item.getEntryId() + "] error adding " + item + " to " + player, e);
			}
		}

		if (rewarded.size() > 0)
			RewardServiceDAO.storeReceived(rewarded, System.currentTimeMillis());
	}

	private boolean sendRewardItem(Player player, RewardEntryItem item) {
		if (DataManager.ITEM_DATA.getItemTemplate(item.getId()) == null)
			return false;

		int itemId = 0;
		long kinahCount = 0, itemCount = 0;
		if (item.getId() == ItemId.KINAH) {
			kinahCount = item.getCount();
		} else {
			itemId = item.getId();
			itemCount = item.getCount();
		}

		return SystemMailService.sendMail("$$CASH_ITEM_MAIL", player.getName(), item.getId() + ", " + item.getCount(),
			"0, " + (System.currentTimeMillis() / 1000) + ",", itemId, itemCount, kinahCount, LetterType.BLACKCLOUD);
	}

	private boolean executeRewardAction(Player player, RewardEntryItem rewardItem) {
		switch (rewardItem.getId()) {
			case 1:
				return MaxLevelReward.reward(player);
			default:
				return false;
		}
	}

	public static class MaxLevelReward {

		private static Set<Integer> pendingAscension = new CopyOnWriteArraySet<>();

		public static boolean isPendingAscension(Player player) {
			return pendingAscension.contains(player.getObjectId());
		}

		public static boolean reward(Player player) {
			if (!player.getCommonData().isDaeva()) {
				if (!pendingAscension.add(player.getObjectId()))
					return false;
				if (player.getLevel() < 9)
					player.getCommonData().setLevel(9); // reaching level 9 starts the ascension quest
				int questNpcId = player.getRace() == Race.ELYOS ? 790001 : 203550; // Pernos / Munin
				int questId = player.getRace() == Race.ELYOS ? 1006 : 2008;
				int questVar = player.getRace() == Race.ELYOS ? 5 : 6;
				QuestState qs = player.getQuestStateList().getQuestState(questId);
				if (qs.getStatus() != QuestStatus.REWARD) { // class selection is complete at this point
					if (qs.getStatus() == QuestStatus.COMPLETE) { // if player switched back to a starting class (GM command or DB change)
						qs.setStatus(QuestStatus.START);
						PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.ADD, qs));
					}
					if (qs.getQuestVars().getQuestVars() != questVar)
						qs.setQuestVar(questVar);
					PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.UPDATE, qs));
				}
				VisibleObject questNpc = player.getKnownList().findObject(questNpcId);
				if (questNpc == null || PositionUtil.getDistance(player, questNpc) >= 20)
					TeleportService.sendTeleportRequest(player, questNpcId); // completing the quest (updates daeva status) calls the reward method again
			} else {
				int maxLevel = DataManager.PLAYER_EXPERIENCE_TABLE.getMaxLevel() - 1; // max is 66
				if (player.getLevel() >= maxLevel)
					return false;
				pendingAscension.remove(player.getObjectId());
				addBasicGear(player);
				player.getCommonData().setLevel(maxLevel);
				String message = ChatUtil.l10n(904804) + " Level " + player.getLevel(); // You receive the following reward: Level 65
				PacketSendUtility.sendMessage(player, message, ChatType.BRIGHT_YELLOW);
				TeleportService.sendTeleportRequest(player, player.getRace() == Race.ELYOS ? 798926 : 799225); // Outremus / Richelle for daevanion quests
			}
			return true;
		}

		private static void addBasicGear(Player player) {
			ItemService.addItem(player, 188053624, 10, true); // Unified Return Scroll Bundle
			switch (player.getPlayerClass()) { // weapons
				case GLADIATOR:
					ItemService.addItem(player, 101300728, 1, true); // Transient Lance (14 days)
					break;
				case TEMPLAR:
					ItemService.addItem(player, 100900749, 1, true); // Transient Greatsword (14 days)
					break;
				case ASSASSIN:
					ItemService.addItem(player, 100000993, 1, true); // Transient Brand (14 days)
					ItemService.addItem(player, 100200882, 1, true); // Transient Dirk (14 days)
					break;
				case RANGER:
					ItemService.addItem(player, 101700795, 1, true); // Transient Bow (14 days)
					break;
				case SORCERER:
					ItemService.addItem(player, 100600830, 1, true); // Transient Tome (14 days)
					break;
				case SPIRIT_MASTER:
					ItemService.addItem(player, 100500775, 1, true); // Transient Sphere (14 days)
					break;
				case CLERIC:
					ItemService.addItem(player, 100100755, 1, true); // Transient Warhammer (14 days)
					ItemService.addItem(player, 115001049, 1, true); // Transient Shield (14 days)
					break;
				case CHANTER:
					ItemService.addItem(player, 101500778, 1, true); // Transient Staff (14 days)
					break;
				case RIDER:
					ItemService.addItem(player, 102101083, 1, true); // Atreian Faithful Cipher-Blade
					break;
				case GUNNER:
					ItemService.addItem(player, 101800899, 2, true); // Transient Pistol (14 days)
					break;
				case BARD:
					ItemService.addItem(player, 102000923, 1, true); // Transient Harp (14 days)
					break;
			}
			switch (player.getPlayerClass()) { // armor
				case GLADIATOR:
				case TEMPLAR:
					ItemService.addItem(player, 110601053, 1, true); // Transient Breastplate (14 days)
					ItemService.addItem(player, 111601030, 1, true); // Transient Gauntlets (14 days)
					ItemService.addItem(player, 112601003, 1, true); // Transient Shoulderplates (14 days)
					ItemService.addItem(player, 113601014, 1, true); // Transient Greaves (14 days)
					ItemService.addItem(player, 114601010, 1, true); // Transient Sabatons (14 days)
					break;
				case ASSASSIN:
				case RANGER:
				case GUNNER:
					ItemService.addItem(player, 110301102, 1, true); // Transient Jerkin (14 days)
					ItemService.addItem(player, 111301057, 1, true); // Transient Vambrace (14 days)
					ItemService.addItem(player, 112301002, 1, true); // Transient Shoulderguards (14 days)
					ItemService.addItem(player, 113301074, 1, true); // Transient Breeches (14 days)
					ItemService.addItem(player, 114301109, 1, true); // Transient Boots (14 days)
					break;
				case SORCERER:
				case SPIRIT_MASTER:
				case BARD:
					ItemService.addItem(player, 110101165, 1, true); // Transient Tunic (14 days)
					ItemService.addItem(player, 111101056, 1, true); // Transient Gloves (14 days)
					ItemService.addItem(player, 112101014, 1, true); // Transient Pauldrons (14 days)
					ItemService.addItem(player, 113101069, 1, true); // Transient Leggings (14 days)
					ItemService.addItem(player, 114101097, 1, true); // Transient Shoes (14 days)
					break;
				case CLERIC:
				case CHANTER:
				case RIDER:
					ItemService.addItem(player, 110501071, 1, true); // Transient Hauberk (14 days)
					ItemService.addItem(player, 111501041, 1, true); // Transient Handguards (14 days)
					ItemService.addItem(player, 112500990, 1, true); // Transient Spaulders (14 days)
					ItemService.addItem(player, 113501049, 1, true); // Transient Chausses (14 days)
					ItemService.addItem(player, 114501057, 1, true); // Transient Brogans (14 days)
					break;
			}
		}
	}
}
