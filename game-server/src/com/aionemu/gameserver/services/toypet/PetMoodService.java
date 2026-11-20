package com.aionemu.gameserver.services.toypet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetMoodAction;
import com.aionemu.gameserver.model.gameobjects.PetMoodRequest;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FUNCTIONAL_PET;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer
 */
public class PetMoodService {

	private static final Logger log = LoggerFactory.getLogger(PetMoodService.class);

	public static void checkMood(Pet pet, PetMoodRequest request, int shuggleEmotion) {
		switch (request) {
			case STATUS:
				startCheckingMood(pet);
				break;
			case SOCIAL:
				interactWithPet(pet, shuggleEmotion);
				break;
			case PRESENT:
				requestPresent(pet);
				break;
			case CANCEL:
				// the client only stops the mood animation, the next status check re-enables it
				break;
		}
	}

	/**
	 * @param pet
	 */
	private static void requestPresent(Pet pet) {
		if (pet.getCommonData().getMoodPoints(false) < 9000) {
			log.warn("Requested present before mood fill up: {}", pet.getMaster().getName());
			return;
		}

		if (pet.getCommonData().getGiftRemainingTime() > 0) {
			AuditLogger.log(pet.getMaster(), "tried to get gift of pet " + pet.getObjectId() + " during CD");
			return;
		}

		if (pet.getMaster().getInventory().isFull()) {
			PacketSendUtility.sendPacket(pet.getMaster(), SM_SYSTEM_MESSAGE.STR_WAREHOUSE_FULL_INVENTORY());
			return;
		}

		int itemId = pet.getObjectTemplate().getConditionReward();
		if (itemId != 0) {
			ItemService.addItem(pet.getMaster(), itemId, 1);
		}
		pet.getCommonData().clearMoodStatistics();
		pet.getCommonData().setGiftCdStarted(System.currentTimeMillis());
		PacketSendUtility.sendPacket(pet.getMaster(), SM_FUNCTIONAL_PET.mood(pet, PetMoodAction.PRESENT, 0));
		PacketSendUtility.sendPacket(pet.getMaster(), SM_FUNCTIONAL_PET.mood(pet, PetMoodAction.UPDATE, 0));
	}

	/**
	 * @param pet
	 * @param shuggleEmotion
	 */
	private static void interactWithPet(Pet pet, int shuggleEmotion) {
		if (pet.getCommonData() != null) {
			if (pet.getCommonData().increaseShuggleCounter()) {
				PacketSendUtility.sendPacket(pet.getMaster(), SM_FUNCTIONAL_PET.mood(pet, PetMoodAction.SOCIAL, shuggleEmotion));
			}
		}
	}

	/**
	 * @param pet
	 */
	private static void startCheckingMood(Pet pet) {
		if (pet.getCommonData().getMoodPoints(false) < 9000 && pet.getCommonData().getMoodRemainingTime() == 0)
			PacketSendUtility.sendPacket(pet.getMaster(), SM_FUNCTIONAL_PET.mood(pet, PetMoodAction.STATUS, 0));
	}

}
