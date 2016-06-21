package com.aionemu.gameserver.services.reward;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.RewardServiceDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.model.templates.rewards.RewardEntryItem;
import com.aionemu.gameserver.services.mail.SystemMailService;

import javolution.util.FastTable;

/**
 * @author KID
 */
public class WebRewardService {

	private static WebRewardService instance = new WebRewardService();
	private static final Logger log = LoggerFactory.getLogger(WebRewardService.class);

	public static WebRewardService getInstance() {
		return instance;
	}

	private WebRewardService() {
	}

	public void verify(Player player) {
		FastTable<RewardEntryItem> list = DAOManager.getDAO(RewardServiceDAO.class).getAvailable(player.getObjectId());
		if (list.size() == 0)
			return;

		FastTable<Integer> rewarded = new FastTable<>();

		for (RewardEntryItem item : list) {
			if (DataManager.ITEM_DATA.getItemTemplate(item.getId()) == null) {
				log.warn("[RewardController][" + item.getEntryId() + "] item ID " + item.getId() + " for " + player
					+ " is invalid and could therefore not be added.");
				continue;
			}

			try {
				int itemId;
				long kinahCount, itemCount;
				if (item.getId() == ItemId.KINAH.value()) {
					itemId = 0;
					itemCount = 0;
					kinahCount = item.getCount();
				} else {
					itemId = item.getId();
					itemCount = item.getCount();
					kinahCount = 0;
				}

				if (!SystemMailService.getInstance().sendMail("$$CASH_ITEM_MAIL", player.getName(), item.getId() + ", " + item.getCount(),
					"0, " + (System.currentTimeMillis() / 1000) + ",", itemId, itemCount, kinahCount, LetterType.BLACKCLOUD)) {
					continue;
				}

				log.info("[RewardController][" + item.getEntryId() + "] player " + player.getName() + " has received (" + item.getCount() + ")" + item.getId()
					+ ".");
				rewarded.add(item.getEntryId());
			} catch (Exception e) {
				log.error(
					"[RewardController][" + item.getEntryId() + "] failed to add item (" + item.getCount() + ")" + item.getId() + " to " + player.getObjectId(),
					e);
				continue;
			}
		}

		if (rewarded.size() > 0) {
			DAOManager.getDAO(RewardServiceDAO.class).uncheckAvailable(rewarded);
		}
	}
}
