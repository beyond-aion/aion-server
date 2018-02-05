package com.aionemu.gameserver.services.reward;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.services.mail.SystemMailService;

/**
 * Created on 29.05.2016
 * 
 * @author Estrayl
 * @since AION 4.8
 */
public class PromotionKitService {

	private static final PromotionKitService INSTANCE = new PromotionKitService();
	private final Map<Integer, List<RewardItem>> itemMap = new LinkedHashMap<>();

	public static PromotionKitService getInstance() {
		return INSTANCE;
	}

	private PromotionKitService() {
		itemMap.put(1, new ArrayList<>());
		itemMap.put(20, new ArrayList<>());
		itemMap.put(25, new ArrayList<>());
		itemMap.put(35, new ArrayList<>());
		itemMap.put(50, new ArrayList<>());

		itemMap.get(1).add(new RewardItem(169610056, 1)); // [Title Card] Novice of Atreia – 30-day pass
		itemMap.get(20).add(new RewardItem(188054100, 1)); // Bronze Coin Box
		itemMap.get(20).add(new RewardItem(125001832, 1)); // Experienced Lepharist Veil
		itemMap.get(20).add(new RewardItem(122000449, 1)); // Ghost Rose Quartz Ring
		itemMap.get(20).add(new RewardItem(122000451, 1)); // Ghost Crystal Ring
		itemMap.get(20).add(new RewardItem(120015052, 1)); // Prestigious Magic Earrings
		itemMap.get(20).add(new RewardItem(120015051, 1)); // Prestigious Combat Earrings
		itemMap.get(20).add(new RewardItem(123000879, 1)); // Morai's Belt
		itemMap.get(25).add(new RewardItem(190100064, 1)); // Flying Pagati (30 days)
		itemMap.get(25).add(new RewardItem(164002272, 25)); // [Event] Enduring Greater Raging Wind Scroll
		itemMap.get(25).add(new RewardItem(162000039, 25)); // Divine Wind Serum
		itemMap.get(25).add(new RewardItem(162002018, 25)); // [Event] Wormwood Dish
		itemMap.get(35).add(new RewardItem(188054101, 1)); // Silver Coin Box
		itemMap.get(50).add(new RewardItem(187060075, 1));
		itemMap.get(50).add(new RewardItem(121000815, 1));
		itemMap.get(50).add(new RewardItem(120000901, 1));
		itemMap.get(50).add(new RewardItem(122001038, 1));
		itemMap.get(50).add(new RewardItem(161001001, 5));
		itemMap.get(50).add(new RewardItem(188053526, 5));
	}

	public void onLevelUp(Player player, int fromLevel, int toLevel) {
		for (int level = fromLevel; level <= toLevel; level++) {
			if (!itemMap.containsKey(level))
				continue;
			for (RewardItem e : itemMap.get(level)) {
				SystemMailService.getInstance().sendMail("Beyond Aion", player.getName(), "Promotion Kit",
					"Greetings Daeva!\n\n"
						+ "In gratitude for your decision to level your character during our promotion phase we prepared an additional item pack.\n\n"
						+ "Enjoy your stay on Beyond Aion!",
					e.getId(), e.getCount(), 0, LetterType.EXPRESS);
			}
		}
	}
}
