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
public class StarterKitService {

	private static final StarterKitService INSTANCE = new StarterKitService();
	private final Map<Integer, List<RewardItem>> itemMap = new LinkedHashMap<>();

	public static StarterKitService getInstance() {
		return INSTANCE;
	}

	private StarterKitService() {
		itemMap.put(1, new ArrayList<>());
		itemMap.put(20, new ArrayList<>());
		itemMap.put(25, new ArrayList<>());
		itemMap.put(35, new ArrayList<>());
		itemMap.put(50, new ArrayList<>());
		itemMap.put(60, new ArrayList<>());

		itemMap.get(1).add(new RewardItem(169610056, 1)); // [Title Card] Novice of Atreia – 30-day pass
		itemMap.get(20).add(new RewardItem(188054100, 1)); // Bronze Coin Box
		itemMap.get(20).add(new RewardItem(125001832, 1)); // Experienced Lepharist Veil
		itemMap.get(20).add(new RewardItem(122000449, 1)); // Ghost Rose Quartz Ring
		itemMap.get(20).add(new RewardItem(122000451, 1)); // Ghost Crystal Ring
		itemMap.get(20).add(new RewardItem(120015052, 1)); // Prestigious Magic Earrings
		itemMap.get(20).add(new RewardItem(120015051, 1)); // Prestigious Combat Earrings
		itemMap.get(20).add(new RewardItem(123000879, 1)); // Morai's Belt
		itemMap.get(25).add(new RewardItem(190100032, 1)); // Pagati Ironhide
		itemMap.get(25).add(new RewardItem(164002272, 25)); // [Event] Enduring Greater Raging Wind Scroll
		itemMap.get(25).add(new RewardItem(162000039, 25)); // Divine Wind Serum
		itemMap.get(25).add(new RewardItem(162002018, 25)); // [Event] Wormwood Dish
		itemMap.get(35).add(new RewardItem(188054101, 1)); // Silver Coin Box
		itemMap.get(35).add(new RewardItem(169620082, 1)); // Gathering Boost Charm II - 100%
		itemMap.get(35).add(new RewardItem(169620094, 1)); // Crafting Boost Charm III - 100%
		itemMap.get(50).add(new RewardItem(121000815, 1)); // Lonely Diamond Necklace
		itemMap.get(50).add(new RewardItem(120000901, 1)); // Lonely Diamond Earrings
		itemMap.get(50).add(new RewardItem(122001038, 1)); // Lonely Diamond Ring
		itemMap.get(50).add(new RewardItem(188053624, 10)); // Return Scroll Bundle
		itemMap.get(50).add(new RewardItem(161001001, 5)); // Revival Stone
		itemMap.get(60).add(new RewardItem(169620072, 1)); // AP Boost Charm II - 30%
		itemMap.get(60).add(new RewardItem(162002030, 100)); // Event] Premium Restoration Serum
		itemMap.get(60).add(new RewardItem(162000107, 50)); // Saam King's Herbs
		itemMap.get(60).add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		itemMap.get(60).add(new RewardItem(188053783, 5)); // Stigma Sack
	}

	public void onLevelUp(Player player, int fromLevel, int toLevel) {
		for (int level = fromLevel; level <= toLevel; level++) {
			if (!itemMap.containsKey(level))
				continue;
			for (RewardItem e : itemMap.get(level)) {
				SystemMailService.sendMail("Beyond Aion", player.getName(), "Starter Kit",
					"Greetings Daeva!\n\n"
						+ "In gratitude for your decision to join our server, we would like to support you with an additional item pack during the leveling.\n\n"
						+ "Enjoy your stay on Beyond Aion!",
					e.getId(), e.getCount(), 0, LetterType.EXPRESS);
			}
		}
	}
}
