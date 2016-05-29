package com.aionemu.gameserver.services.rewardPackage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;
import javolution.util.FastTable;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.mail.SystemMailService;

/**
 * Created on 29.05.2016
 * 
 * @author Estrayl
 * @since AION 4.8
 */
public class PromotionKitService {

	private static final PromotionKitService INSTANCE = new PromotionKitService();
	private final Map<Integer, List<PackageItem>> itemMap = new FastMap<>();
	private LocalDateTime minCreationTime;

	public static PromotionKitService getInstance() {
		return INSTANCE;
	}

	private PromotionKitService() {
		if (!CustomConfig.ENABLE_PROMOTION_KIT)
			return;
		
		String[] split = CustomConfig.PROMOTION_KIT_MIN_CREATION_DATE.split(" ");
		String[] date = split[0].split("\\.");
		String[] time = split[1].split("\\:");

		try {
			minCreationTime = LocalDateTime.of(Integer.parseInt(date[2]), Integer.parseInt(date[1]), Integer.parseInt(date[0]), Integer.parseInt(time[0]),
				Integer.parseInt(time[1]));
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			LoggerFactory.getLogger(PromotionKitService.class).error("Exception occured while initializing minimal creation time!", e);
		}

		itemMap.put(1, new FastTable<>());
		itemMap.put(25, new FastTable<>());
		itemMap.put(50, new FastTable<>());

		itemMap.get(1).add(new PackageItem(169610056, 1));
		itemMap.get(25).add(new PackageItem(190100064, 1));
		itemMap.get(25).add(new PackageItem(164002272, 25));
		itemMap.get(25).add(new PackageItem(162000026, 25));
		itemMap.get(25).add(new PackageItem(162002018, 25));
		itemMap.get(50).add(new PackageItem(187060133, 1));
		itemMap.get(50).add(new PackageItem(188053289, 1));
		itemMap.get(50).add(new PackageItem(161001001, 5));
		itemMap.get(50).add(new PackageItem(188053526, 5));
	}

	public void onLevelUp(Player player) {
		if (itemMap == null || minCreationTime == null || itemMap.isEmpty())
			return;
		if (!itemMap.containsKey((int) player.getLevel()))
			return;
		if (player.getPlayerAccount().getPlayerAccountData(player.getObjectId()).getCreationDate().toLocalDateTime().isBefore(minCreationTime))
			return;
		for (PackageItem e : itemMap.get((int) player.getLevel())) {
			SystemMailService.getInstance().sendMail("Beyond Aion",	player.getName(),	"Promotion Kit",
				"Greetings Daeva!\n\n" + "In gratitude for your decision to join our server we prepared an additional item pack.\n\n"
					+ "Enjoy your stay on Beyond Aion!", e.getId(), e.getCount(), 0, LetterType.EXPRESS);
		}
	}
}
