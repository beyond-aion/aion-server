package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.cache.HTMLCache;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.dao.SurveyControllerDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.survey.SurveyItem;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author KID
 */
public class SurveyService {

	private static final Logger log = LoggerFactory.getLogger(SurveyService.class);
	private final Map<Integer, SurveyItem> activeItems = new LinkedHashMap<>();

	private SurveyService() {
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new TaskUpdate(), 2000, SecurityConfig.SURVEY_DELAY * 60000);
	}

	public boolean isActive(Player player, int survId) {
		boolean avail = activeItems.containsKey(survId);
		if (avail)
			requestSurvey(player, survId);

		return avail;
	}

	public void requestSurvey(Player player, int survId) {

		SurveyItem item = activeItems.get(survId);
		if (item == null || item.ownerId != player.getObjectId()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_FIND_POLL());
			return;
		}

		ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(item.itemId);
		if (template == null) {
			return;
		}
		if (player.getInventory().isFull(template.getExtraInventoryId())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_FULL_INVENTORY());
			log.warn("[SurveyController] player " + player.getName() + " tried to receive item with full inventory.");
			return;
		}
		if (SurveyControllerDAO.useItem(item.uniqueId)) {

			ItemService.addItem(player, item.itemId, item.count);
			if (item.itemId == ItemId.KINAH)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_POLL_REWARD_MONEY(item.count));
			else if (item.count == 1)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_POLL_REWARD_ITEM(template.getL10n()));
			else
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_POLL_REWARD_ITEM_MULTI(item.count, template.getL10n()));

			activeItems.remove(survId);
		}
	}

	public void taskUpdate() {
		List<SurveyItem> newList = SurveyControllerDAO.getAllUnused();
		if (newList.size() == 0)
			return;

		List<Integer> players = new ArrayList<>();
		int cnt = 0;
		for (SurveyItem survey : newList) {
			if (activeItems.putIfAbsent(survey.uniqueId, survey) == null) {
				cnt++;
				if (!players.contains(survey.ownerId))
					players.add(survey.ownerId);
			}
		}
		log.info("[SurveyController] found new " + cnt + " items for " + players.size() + " players.");
		for (int ownerId : players) {
			Player player = World.getInstance().getPlayer(ownerId);
			if (player != null) {
				showAvailable(player);
			}
		}
	}

	public void showAvailable(Player player) {
		for (SurveyItem item : this.activeItems.values()) {
			if (item.ownerId != player.getObjectId())
				continue;

			String context = HTMLCache.getInstance().getHTML("surveyTemplate.xhtml");
			context = context.replace("%itemid%", item.itemId + "");
			context = context.replace("%itemcount%", item.count + "");
			context = context.replace("%html%", item.html);
			context = context.replace("%radio%", item.radio);

			HTMLService.sendData(player, item.uniqueId, context);
		}
	}

	public class TaskUpdate implements Runnable {

		@Override
		public void run() {
			log.info("[SurveyController] update task start.");
			taskUpdate();
		}
	}

	private static class SingletonHolder {

		protected static final SurveyService instance = new SurveyService();
	}

	public static final SurveyService getInstance() {
		return SingletonHolder.instance;
	}
}
