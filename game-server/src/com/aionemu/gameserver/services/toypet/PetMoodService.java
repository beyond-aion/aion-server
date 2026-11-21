package com.aionemu.gameserver.services.toypet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetMoodRequest;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PET;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * Pet mood: a summoned pet gains 100 points per minute. Every 10 minutes the player can check the pet's mood and then socialize with it once
 * within 30 seconds, for 300, 600 or 900 points depending on the pet's reaction. A full mood (9000 points) lets the player take a present once
 * per hour, which empties the mood again.
 * <p>
 * Like on retail, the mood is not stored in the database: the server remembers the mood of each player's last dismissed pet for 10 minutes, so
 * re-summoning that pet in time (also after a relog) keeps it, while any other pet starts with an empty mood.
 *
 * @author ATracer
 */
public class PetMoodService {

	public static final int POINTS_PER_MINUTE = 100;
	private static final int NO_PRESENT = -1;
	private static final long KEEP_MOOD_MILLIS = 600_000;
	private static final long CLEANUP_INTERVAL_MILLIS = 60_000;
	private static final Map<Integer, DismissedPetMood> dismissedPetMoods = new ConcurrentHashMap<>();

	static {
		ThreadPoolManager.getInstance().scheduleAtFixedRate(PetMoodService::removeExpiredMoods, CLEANUP_INTERVAL_MILLIS, CLEANUP_INTERVAL_MILLIS);
	}

	private record DismissedPetMood(int petTemplateId, int moodPoints, long moodCheckTime, long presentTime, long dismissTime) {

		boolean isExpired(long now) {
			return now - dismissTime >= KEEP_MOOD_MILLIS;
		}
	}

	private static void removeExpiredMoods() {
		long now = System.currentTimeMillis();
		dismissedPetMoods.values().removeIf(mood -> mood.isExpired(now));
	}

	/**
	 * Remembers the mood of a dismissed pet, replacing the previous pet's.
	 */
	public static void onDismiss(Player player, PetCommonData pet) {
		dismissedPetMoods.put(player.getObjectId(), new DismissedPetMood(pet.getTemplateId(), pet.getMoodPoints(), pet.getMoodCheckTime(),
			pet.getPresentTime(), System.currentTimeMillis()));
	}

	/**
	 * Restores the mood of the summoned pet if it is the last dismissed one and was dismissed less than 10 minutes ago, otherwise empties it.
	 */
	public static void onSummon(Player player, PetCommonData pet) {
		DismissedPetMood mood = dismissedPetMoods.remove(player.getObjectId());
		pet.resetMood();
		if (mood != null && mood.petTemplateId() == pet.getTemplateId() && !mood.isExpired(System.currentTimeMillis())) {
			pet.setMoodPoints(mood.moodPoints());
			pet.setMoodCheckTime(mood.moodCheckTime());
			pet.setPresentTime(mood.presentTime());
		}
	}

	public static void checkMood(Pet pet, PetMoodRequest request, int emotion) {
		switch (request) {
			case STATUS -> startMoodCheck(pet);
			case SOCIAL -> socialize(pet, emotion);
			case CANCEL -> pet.getCommonData().cancelSocial();
			case PRESENT -> requestPresent(pet);
		}
	}

	/**
	 * Called once per minute while the pet is summoned. A full mood bar is sent again on every tick, which keeps the client's present cooldown in
	 * sync.
	 */
	public static void onMinutePassed(Pet pet) {
		if (pet.getCommonData().addMoodPoints(POINTS_PER_MINUTE))
			PacketSendUtility.sendPacket(pet.getMaster(), SM_PET.moodUpdate(pet.getCommonData()));
	}

	private static void startMoodCheck(Pet pet) {
		if (pet.getCommonData().startMoodCheck())
			PacketSendUtility.sendPacket(pet.getMaster(), SM_PET.moodStatus(Rnd.get(0, 2)));
	}

	private static void socialize(Pet pet, int emotion) {
		PetCommonData commonData = pet.getCommonData();
		if (!commonData.useSocial())
			return;
		int reaction = Rnd.get(0, 8);
		commonData.addMoodPoints(reaction == 8 ? 900 : reaction % 2 == 0 ? 300 : 600);
		PacketSendUtility.sendPacket(pet.getMaster(), SM_PET.moodSocial(reaction, commonData.getMoodPoints(), emotion));
	}

	private static void requestPresent(Pet pet) {
		Player player = pet.getMaster();
		PetCommonData commonData = pet.getCommonData();
		if (commonData.isPresentOnCooldown()) {
			AuditLogger.log(player, "tried to get the present of pet " + pet.getObjectId() + " during its cooldown");
			return;
		}
		int presentItemId = NO_PRESENT;
		if (commonData.getMoodPoints() >= PetCommonData.MAX_MOOD_POINTS) {
			if (player.getInventory().isFull()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_FULL_INVENTORY());
				return;
			}
			int itemId = pet.getObjectTemplate().getConditionReward();
			if (itemId != 0 && ItemService.addItem(player, itemId, 1) == 0)
				presentItemId = itemId;
			commonData.setMoodPoints(0);
			commonData.setPresentTime(System.currentTimeMillis());
			PacketSendUtility.sendPacket(player, SM_PET.moodUpdate(commonData));
		}
		// -1 tells the client that the pet has nothing to give, because the mood isn't full yet
		PacketSendUtility.sendPacket(player, SM_PET.moodPresent(presentItemId));
	}

}
