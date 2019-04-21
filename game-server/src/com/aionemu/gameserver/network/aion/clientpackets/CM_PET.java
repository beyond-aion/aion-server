package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PET;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.NameRestrictionService;
import com.aionemu.gameserver.services.toypet.PetAdoptionService;
import com.aionemu.gameserver.services.toypet.PetMoodService;
import com.aionemu.gameserver.services.toypet.PetService;
import com.aionemu.gameserver.services.toypet.PetSpawnService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author M@xx, xTz
 */
public class CM_PET extends AionClientPacket {

	private PetAction action;
	private int templateId;
	private int objectId;
	private String petName;
	private int decorationId;
	private int eggObjId;
	private int count;
	private int subType;
	private int emotionId;
	private int actionType;
	private int dopingItemId;
	private int dopingAction;
	private int dopingSlot1;
	private int dopingSlot2;
	private int activateSpecialFunction;

	@SuppressWarnings("unused")
	private int unk2, unk3, unk5, unk6;

	public CM_PET(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		action = PetAction.getActionById(readUH());
		switch (action) {
			case ADOPT:
				eggObjId = readD();
				templateId = readD();
				unk2 = readUC();
				unk3 = readD();
				decorationId = readD();
				unk5 = readD();
				unk6 = readD();
				petName = readS();
				break;
			case SURRENDER:
			case SPAWN:
			case DISMISS:
				templateId = readD();
				break;
			case FOOD:
				actionType = readD();
				if (actionType == 3 || actionType == 4) { // auto loot (3), or auto sell items (4)
					activateSpecialFunction = readD();
					readD(); // always 0
					readD(); // always 0
				} else if (actionType == 2) {
					dopingAction = readD();
					if (dopingAction == 0) { // add item
						dopingItemId = readD();
						dopingSlot1 = readD();
					} else if (dopingAction == 1) { // remove item
						dopingSlot1 = readD();
						dopingItemId = readD();
					} else if (dopingAction == 2) { // switch items in two occupied slots
						dopingSlot1 = readD();
						dopingSlot2 = readD();
					} else if (dopingAction == 3) { // use doping
						dopingItemId = readD();
						dopingSlot1 = readD();
					}
					// TODO: PetBuffs go here.
					// Commented out now, no crash if handled in else clause
					// else if (actionType == 5) {
					// readD(); // cherry count or buff enabled? Read value = 1
					// }
				} else {
					objectId = readD();
					count = readD();
					unk2 = readD();
				}
				break;
			case RENAME:
				objectId = readD();
				petName = readS();
				break;
			case MOOD:
				subType = readD();
				emotionId = readD();
				break;
			case EXTEND_EXPIRATION: // extend expiration date
				eggObjId = readD(); // itemObjId
				objectId = readD(); // petObjId
				break;
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;

		Pet pet = player.getPet();
		switch (action) {
			case ADOPT:
				if (!NameRestrictionService.isValidPetName(petName) || NameRestrictionService.isForbidden(petName))
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PET_NOT_AVALIABE_NAME());
				else
					PetAdoptionService.adoptPet(player, eggObjId, templateId, petName, decorationId);
				break;
			case EXTEND_EXPIRATION:
				// for now we will do nothing, cause expiration-time is shitty
				break;
			case SURRENDER:
				PetAdoptionService.surrenderPet(player, templateId);
				break;
			case SPAWN:
				PetSpawnService.summonPet(player, templateId);
				break;
			case DISMISS:
				if (pet != null)
					pet.getController().delete();
				break;
			case FOOD:
				if (pet == null)
					return;
				if (actionType == 2) { // Pet doping
					PetService.getInstance().useDoping(pet, dopingAction, dopingItemId, dopingSlot1, dopingSlot2);
				} else if (actionType == 3) { // Pet looting
					PetService.getInstance().activateLoot(pet, activateSpecialFunction != 0);
				} else if (actionType == 4) { // Pet auto sell items
					PetService.getInstance().activateAutoSell(pet, activateSpecialFunction != 0);
				} else if (objectId == 0) {
					pet.getCommonData().setCancelFeed(true);
					PacketSendUtility.sendPacket(player, new SM_PET(4, 0, 0, player.getPet()));
					PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.END_FEEDING, 0, player.getObjectId()));
				} else if (!pet.getCommonData().isFeedingTime()) {
					PacketSendUtility.sendPacket(player, new SM_PET(8, objectId, count, player.getPet()));
				} else
					PetService.getInstance().removeObject(objectId, count, player);
				break;
			case RENAME:
				if (!NameRestrictionService.isValidPetName(petName) || NameRestrictionService.isForbidden(petName))
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PET_NOT_AVALIABE_NAME());
				else
					PetService.getInstance().renamePet(player, petName);
				break;
			case MOOD:
				if (pet != null && (subType == 0 && pet.getCommonData().getMoodRemainingTime() == 0
					|| (subType == 3 && pet.getCommonData().getGiftRemainingTime() == 0) || emotionId != 0)) {
					PetMoodService.checkMood(pet, subType, emotionId);
				}
				break;
		}
	}

}
