package com.aionemu.gameserver.services.toypet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PET;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer
 */
public class PetMoodService {

	private static final Logger log = LoggerFactory.getLogger(PetMoodService.class);

	public static void checkMood(Pet pet, int type, int snuggleEmotion) {
		switch (type) {
			case 0 -> startCheckingMood(pet);
			case 1 -> interactWithPet(pet, snuggleEmotion);
			case 3 -> requestPresent(pet);
		}
	}

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
		pet.getCommonData().clearMoodStatistics();
		PacketSendUtility.sendPacket(pet.getMaster(), new SM_PET(pet, 4, 0));
		PacketSendUtility.sendPacket(pet.getMaster(), new SM_PET(pet, 3, 0));
		int itemId = pet.getObjectTemplate().getConditionReward();
		if (itemId != 0) {
			ItemService.addItem(pet.getMaster(), pet.getObjectTemplate().getConditionReward(), 1);
		}
	}

	private static void interactWithPet(Pet pet, int snuggleEmotion) {
		if (pet.getCommonData() != null) {
			if (pet.getCommonData().increaseShuggleCounter()) {
				PacketSendUtility.sendPacket(pet.getMaster(), new SM_PET(pet, 2, snuggleEmotion));
				PacketSendUtility.sendPacket(pet.getMaster(), new SM_PET(pet, 4, 0)); // Update progress immediately
			}
		}
	}

	private static void startCheckingMood(Pet pet) {
		PacketSendUtility.sendPacket(pet.getMaster(), new SM_PET(pet, 0, 0));
	}
}