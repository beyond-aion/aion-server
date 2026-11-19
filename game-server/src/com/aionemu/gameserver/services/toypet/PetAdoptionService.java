package com.aionemu.gameserver.services.toypet;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.AdoptPetAction;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FUNCTIONAL_PET;
import com.aionemu.gameserver.services.NameRestrictionService;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public class PetAdoptionService {

	/**
	 * Create a pet for player (with validation)
	 * 
	 * @return The error code for {@link SM_FUNCTIONAL_PET#adoptionResult(int)}, or 0 on success.
	 */
	public static int adoptPet(Player player, int eggObjId, int petId, String name, int decorationId, int color, int face, int head) {
		if (!NameRestrictionService.isValidPetName(name) || NameRestrictionService.isForbidden(name))
			return SM_FUNCTIONAL_PET.ADOPTION_INVALID_NAME;
		if (player.getPetList().hasSameName(name))
			return SM_FUNCTIONAL_PET.ADOPTION_NAME_ALREADY_USED;
		if (player.getPetList().hasPet(petId))
			return SM_FUNCTIONAL_PET.ADOPTION_INVALID_EGG;
		if (DataManager.PET_DATA.getPetTemplate(petId) == null)
			return SM_FUNCTIONAL_PET.ADOPTION_INVALID_EGG;
		AdoptPetAction adoptPetAction = getAdoptPetAction(player, eggObjId, petId);
		if (adoptPetAction == null)
			return SM_FUNCTIONAL_PET.ADOPTION_INVALID_EGG;
		if (!player.getInventory().decreaseByObjectId(eggObjId, 1))
			return SM_FUNCTIONAL_PET.ADOPTION_EGG_NOT_CONSUMED;
		int expireTimeSeconds =
			adoptPetAction.getExpireMinutes() != 0 ? (int) ((System.currentTimeMillis() / 1000) + adoptPetAction.getExpireMinutes() * 60) : 0;
		addPet(player, petId, name, decorationId, color, face, head, expireTimeSeconds);
		return SM_FUNCTIONAL_PET.ADOPTION_SUCCESS;
	}

	public static void addPet(Player player, int petId, String name, int decorationId, int color, int face, int head, int expireTimeSeconds) {
		name = Util.convertName(name);
		PetCommonData petCommonData = player.getPetList().addPet(player, petId, decorationId, color, face, head, name, expireTimeSeconds);
		PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.adopted(petCommonData));
		ExpireTimerTask.getInstance().registerExpirable(petCommonData, player);
	}

	private static AdoptPetAction getAdoptPetAction(Player player, int eggObjId, int petId) {
		Item egg = player.getInventory().getItemByObjId(eggObjId);
		if (egg == null) {
			AuditLogger.log(player, "tried to adopt pet " + petId + " with missing egg item " + eggObjId);
			return null;
		}
		AdoptPetAction adoptPetAction = egg.getItemTemplate().getActions() == null ? null : egg.getItemTemplate().getActions().getAdoptPetAction();
		if (adoptPetAction == null || adoptPetAction.getPetId() != petId) {
			AuditLogger.log(player, "tried to adopt pet " + petId + " with invalid egg item " + eggObjId);
			return null;
		}
		return adoptPetAction;
	}

	/**
	 * Delete pet
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
