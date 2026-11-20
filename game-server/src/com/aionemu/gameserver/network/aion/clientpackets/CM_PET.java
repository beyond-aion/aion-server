package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.*;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FUNCTIONAL_PET;
import com.aionemu.gameserver.services.toypet.PetAdoptionService;
import com.aionemu.gameserver.services.toypet.PetMoodService;
import com.aionemu.gameserver.services.toypet.PetService;
import com.aionemu.gameserver.services.toypet.PetSpawnService;

/**
 * @author M@xx, xTz
 */
public class CM_PET extends AionClientPacket {

	private PetAction action;
	private PetFunctionAction functionAction;
	private PetDopingAction dopingAction;
	private PetMoodRequest moodRequest;
	private int templateId;
	private int petObjectId;
	private int eggObjId;
	private int feedItemObjectId;
	private int count;
	private String petName;
	private int decorationId;
	private int color;
	private int face;
	private int head;
	private int emotionId;
	private int dopingItemId;
	private int dopingSlot1;
	private int dopingSlot2;
	private boolean activateSpecialFunction;

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
				// 17 byte appearance block: type, color, decoration, face and head attachment ids
				readUC();
				color = readD();
				decorationId = readD();
				face = readD();
				head = readD();
				petName = readS();
				break;
			case SURRENDER:
			case SPAWN:
				templateId = readD();
				break;
			case DISMISS:
				petObjectId = readD();
				break;
			case FOOD:
				functionAction = PetFunctionAction.getById(readD());
				int param1 = readD();
				int param2 = readD();
				int param3 = readD();
				if (functionAction == PetFunctionAction.FEEDING) {
					feedItemObjectId = param1; // 0 cancels the current feeding
					count = param2;
				} else if (functionAction == PetFunctionAction.DOPING) {
					dopingAction = PetDopingAction.getById(param1);
					if (dopingAction == PetDopingAction.ADD || dopingAction == PetDopingAction.USE) {
						dopingItemId = param2;
						dopingSlot1 = param3;
					} else if (dopingAction == PetDopingAction.REMOVE) {
						dopingSlot1 = param2;
						dopingItemId = param3;
					} else if (dopingAction == PetDopingAction.SWITCH) {
						dopingSlot1 = param2;
						dopingSlot2 = param3;
					}
				} else if (functionAction != null) { // autoloot, autosell, buff
					activateSpecialFunction = param1 != 0;
				}
				break;
			case RENAME:
				templateId = readD();
				petName = readS();
				break;
			case MOOD:
				moodRequest = PetMoodRequest.getById(readD());
				emotionId = readD();
				break;
			case EXTEND_EXPIRATION:
				readD(); // item object id
				readD(); // pet template id
				break;
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Pet pet = player.getPet();
		switch (action) {
			case ADOPT:
				int result = PetAdoptionService.adoptPet(player, eggObjId, templateId, petName, decorationId, color, face, head);
				if (result != SM_FUNCTIONAL_PET.ADOPTION_SUCCESS)
					sendPacket(SM_FUNCTIONAL_PET.adoptionResult(result));
				break;
			case EXTEND_EXPIRATION:
				// the item data does not carry the pet id and the extension time for these items
				break;
			case SURRENDER:
				PetAdoptionService.surrenderPet(player, templateId);
				break;
			case SPAWN:
				PetSpawnService.summonPet(player, templateId);
				break;
			case DISMISS:
				if (pet != null && pet.getObjectId() == petObjectId)
					pet.getController().delete();
				break;
			case FOOD:
				if (pet == null || functionAction == null)
					return;
				switch (functionAction) {
					case FEEDING:
						if (pet.getCommonData().getFeedProgress() == null)
							break;
						if (feedItemObjectId == 0) {
							pet.getCommonData().setCancelFeed(true);
							sendPacket(SM_FUNCTIONAL_PET.feeding(PetFeedAction.CANCEL, 0, 0, pet));
							sendPacket(new SM_EMOTION(player, EmotionType.END_FEEDING, 0, player.getObjectId()));
						} else if (pet.getCommonData().getRefeedDelay() > 0) { // not hungry yet
							sendPacket(SM_FUNCTIONAL_PET.feeding(PetFeedAction.REJECTED, feedItemObjectId, count, pet));
						} else
							PetService.getInstance().removeObject(feedItemObjectId, count, player);
						break;
					case DOPING:
						if (dopingAction != null)
							PetService.getInstance().useDoping(pet, dopingAction, dopingItemId, dopingSlot1, dopingSlot2);
						break;
					case AUTOLOOT:
						PetService.getInstance().activateLoot(pet, activateSpecialFunction);
						break;
					case AUTOSELL:
						PetService.getInstance().activateAutoSell(pet, activateSpecialFunction);
						break;
					case BUFF:
						// buff pets are not implemented
						break;
				}
				break;
			case RENAME:
				PetService.getInstance().renamePet(player, templateId, petName);
				break;
			case MOOD:
				if (pet != null && moodRequest != null)
					PetMoodService.checkMood(pet, moodRequest, emotionId);
				break;
		}
	}

}
