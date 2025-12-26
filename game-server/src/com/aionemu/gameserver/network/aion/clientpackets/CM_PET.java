package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Optional;
import java.util.Set;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.PetAction;
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
 * @author SVDNESS
 * @version 4.8 [JDK 25]
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

	public CM_PET(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		action = PetAction.getActionById(readUH());
		switch (action) {
			case ADOPT -> {
				eggObjId = readD();
				templateId = readD();
				readUC();
				readD();
				decorationId = readD();
				readD();
				readD();
				petName = readS();
			}
			case SURRENDER, SPAWN, DISMISS -> templateId = readD();
			case FOOD -> {
				actionType = readD();
				if (actionType == 3 || actionType == 4 || actionType == 5) {
					activateSpecialFunction = readD();
					readD();
					readD();
				} else if (actionType == 2) {
					dopingAction = readD();
					if (dopingAction == 0) {
						dopingItemId = readD();
						dopingSlot1 = readD();
					} else if (dopingAction == 1) {
						dopingSlot1 = readD();
						dopingItemId = readD();
					} else if (dopingAction == 2) {
						dopingSlot1 = readD();
						dopingSlot2 = readD();
					} else if (dopingAction == 3) {
						dopingItemId = readD();
						dopingSlot1 = readD();
					}
				} else {
					objectId = readD();
					count = readD();
					readD();
				}
			}
			case RENAME -> {
				objectId = readD();
				petName = readS();
			}
			case MOOD -> {
				subType = readD();
				emotionId = readD();
			}
		}
	}

	@Override
	protected void runImpl() {
		final var player = getConnection().getActivePlayer();
		if (player == null) {
			return;
		}
		final var pet = player.getPet();
		switch (action) {
			case ADOPT -> {
				if (!NameRestrictionService.isValidPetName(petName) || NameRestrictionService.isForbidden(petName)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PET_NOT_AVALIABE_NAME());
				} else {
					PetAdoptionService.adoptPet(player, eggObjId, templateId, petName, decorationId);
				}
			}
			case SURRENDER -> PetAdoptionService.surrenderPet(player, templateId);
			case SPAWN -> PetSpawnService.summonPet(player, templateId);
			case DISMISS -> Optional.ofNullable(pet).ifPresent(p -> p.getController().delete());
			case FOOD -> {
				if (pet == null) {
					return;
				}
				switch (actionType) {
					case 2 -> PetService.getInstance().useDoping(pet, dopingAction, dopingItemId, dopingSlot1, dopingSlot2); //Допинг.
					case 3 -> PetService.getInstance().activateLoot(pet, activateSpecialFunction != 0); //Лут.
					case 4 -> PetService.getInstance().activateAutoSell(pet, activateSpecialFunction != 0); //Авто-продажа.
					case 5 -> PetService.getInstance().activeCheering(player, activateSpecialFunction == 1); //Баф.
					default -> {
						if (objectId == 0) {
							pet.getCommonData().setCancelFeed(true);
							PacketSendUtility.sendPacket(player, new SM_PET(4, 0, 0, player.getPet()));
							PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.END_FEEDING, 0, player.getObjectId()));
						} else if (pet.getCommonData().getRefeedDelay() > 0) {
							PacketSendUtility.sendPacket(player, new SM_PET(8, objectId, count, player.getPet()));
						} else {
							PetService.getInstance().removeObject(objectId, count, player);
						}
					}
				}
			}
			case RENAME -> {
				if (!NameRestrictionService.isValidPetName(petName) || NameRestrictionService.isForbidden(petName)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PET_NOT_AVALIABE_NAME());
				} else {
					PetService.getInstance().renamePet(player, petName);
				}
			}
			case MOOD -> {
				if (pet != null && (subType == 0 && pet.getCommonData().getMoodRemainingTime() == 0
						|| (subType == 3 && pet.getCommonData().getGiftRemainingTime() == 0) || emotionId != 0)) {
					PetMoodService.checkMood(pet, subType, emotionId);
				}
			}
		}
	}
}