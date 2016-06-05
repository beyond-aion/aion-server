package com.aionemu.gameserver.services.reward;

import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.services.mail.SystemMailService;

import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * Created on 29.05.2016
 * 
 * @author Estrayl
 * @since AION 4.8
 */
public class PromotionKitService {

	private static final PromotionKitService INSTANCE = new PromotionKitService();
	private final Map<Integer, List<RewardItem>> itemMap = new FastMap<>();

	public static PromotionKitService getInstance() {
		return INSTANCE;
	}

	private PromotionKitService() {
		itemMap.put(1, new FastTable<>());
		itemMap.put(25, new FastTable<>());
		itemMap.put(50, new FastTable<>());

		itemMap.get(1).add(new RewardItem(169610056, 1));
		itemMap.get(25).add(new RewardItem(190100064, 1));
		itemMap.get(25).add(new RewardItem(164002272, 25));
		itemMap.get(25).add(new RewardItem(162000039, 25));
		itemMap.get(25).add(new RewardItem(162002018, 25));
		itemMap.get(50).add(new RewardItem(187060075, 1));
		itemMap.get(50).add(new RewardItem(188053289, 1));
		itemMap.get(50).add(new RewardItem(161001001, 5));
		itemMap.get(50).add(new RewardItem(188053526, 5));
	}

	public void onLevelUp(Player player) {
		if (itemMap == null || itemMap.isEmpty())
			return;
		if (!itemMap.containsKey((int) player.getLevel()))
			return;
		for (RewardItem e : itemMap.get((int) player.getLevel())) {
			SystemMailService.getInstance().sendMail("Beyond Aion", player.getName(), "Promotion Kit",
				"Greetings Daeva!\n\n"
					+ "In gratitude for your decision to level your character during our promotion phase we prepared an additional item pack.\n\n"
					+ "Enjoy your stay on Beyond Aion!",
				e.getId(), e.getCount(), 0, LetterType.EXPRESS);
		}
	}
}
