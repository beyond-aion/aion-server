package com.aionemu.gameserver.services.toypet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FUNCTIONAL_PET;
import com.aionemu.gameserver.services.NameRestrictionService;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public class PetAdoptionService {

	private static final Logger log = LoggerFactory.getLogger(PetAdoptionService.class);

	/**
	 * Create a pet for player (with validation)
	 * 
	 * @return 0 on success, 1 for an invalid egg item, 2 if the egg could not be consumed, 3 for an invalid name, 4 if the name is
	 *         already used by another pet
	 */
	public static int adoptPet(Player player, int eggObjId, int petId, String name, int decorationId, int color, int face, int head) {
		if (!NameRestrictionService.isValidPetName(name) || NameRestrictionService.isForbidden(name))
			return 3;
		if (player.getPetList().hasSameName(name))
			return 4;

		Item egg = player.getInventory().getItemByObjId(eggObjId);
		if (egg == null) {
			log.warn("Tried to adopt pet {} with missing egg item {} of {}", petId, eggObjId, player);
			return 1;
		}
		ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(egg.getItemId());

		if (!validateAdoption(player, template, petId))
			return 1;

		int expireTime = template.getActions().getAdoptPetAction().getExpireMinutes() != 0
			? (int) ((System.currentTimeMillis() / 1000) + template.getActions().getAdoptPetAction().getExpireMinutes() * 60) : 0;

		if (!player.getInventory().decreaseByObjectId(eggObjId, 1))
			return 2;

		addPet(player, petId, name, decorationId, color, face, head, expireTime);
		return 0;
	}

	/**
	 * Add pet to player
	 * 
	 * @param player
	 * @param petId
	 * @param name
	 * @param decorationId
	 */
	public static void addPet(Player player, int petId, String name, int decorationId, int expireTime) {
		addPet(player, petId, name, decorationId, 0, 0, 0, expireTime);
	}

	public static void addPet(Player player, int petId, String name, int decorationId, int color, int face, int head, int expireTime) {
		name = Util.convertName(name);
		PetCommonData petCommonData = player.getPetList().addPet(player, petId, decorationId, color, face, head, name, expireTime);
		if (petCommonData != null) {
			PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.adopted(petCommonData));
			ExpireTimerTask.getInstance().registerExpirable(petCommonData, player);
		}
	}

	private static boolean validateAdoption(Player player, ItemTemplate template, int petId) {
		if (template == null || template.getActions() == null || template.getActions().getAdoptPetAction() == null
			|| template.getActions().getAdoptPetAction().getPetId() != petId) {
			return false;
		}
		if (player.getPetList().hasPet(petId)) {
			log.warn("Duplicate pet adoption {} (pet: {})", player, petId);
			return false;
		}
		if (DataManager.PET_DATA.getPetTemplate(petId) == null) {
			log.warn("Trying adopt pet without template. PetId:{}", petId);
			return false;
		}
		return true;
	}

	/**
	 * Delete pet
	 * 
	 * @param player
	 * @param petId
	 */
	public static void surrenderPet(Player player, int petId) {
		surrenderPet(player, petId, false);
	}

	/**
	 * Delete pet. A pet that reached its expiration date is abandoned (form 14) instead of surrendered, so the client shows the
	 * "time is up" message.
	 */
	public static void surrenderPet(Player player, int petId, boolean expired) {
		PetCommonData petCommonData = player.getPetList().deletePet(petId);
		if (petCommonData == null)
			return;
		if (player.getPet() != null && player.getPet().getObjectId() == petCommonData.getObjectId())
			player.getPet().getController().delete();
		PacketSendUtility.sendPacket(player, expired ? SM_FUNCTIONAL_PET.abandoned(petCommonData) : SM_FUNCTIONAL_PET.surrendered(petCommonData));
		IDFactory.getInstance().releaseId(petCommonData.getObjectId());
	}

}
