package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetAction;
import com.aionemu.gameserver.model.gameobjects.PetSpecialFunction;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.templates.pet.PetDopingBag;
import com.aionemu.gameserver.model.templates.pet.PetFunctionType;
import com.aionemu.gameserver.model.templates.pet.PetTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author M@xx, xTz, Rolandas
 */
public class SM_PET extends AionServerPacket {

	private PetAction action;
	private Pet pet;
	private int petObjectId;
	private PetCommonData commonData;
	private String petName;
	private int itemObjectId;
	private Collection<PetCommonData> pets;
	private int count;
	private int subType;
	private int shuggleEmotion;
	private boolean isActing;
	private int lootNpcObjId;
	private int dopeAction;
	private int dopeSlot;
	private byte animationId;

	public SM_PET(int subType, int itemObjectId, int count, Pet pet) {
		this.action = PetAction.FOOD;
		this.subType = subType;
		this.count = count;
		this.itemObjectId = itemObjectId;
		this.commonData = pet.getCommonData();
	}

	public SM_PET(PetAction action) {
		this.action = action;
	}

	public SM_PET(int petObjectId, String petName) {
		this.action = PetAction.RENAME;
		this.petObjectId = petObjectId;
		this.petName = petName;
	}

	public SM_PET(Pet pet) {
		this.action = PetAction.SPAWN;
		this.pet = pet;
	}

	public SM_PET(PetCommonData commonData, boolean isAdopt) {
		this.action = isAdopt ? PetAction.ADOPT : PetAction.SURRENDER;
		this.commonData = commonData;
	}

	public SM_PET(int petId, int petObjectId) {
		this.action = PetAction.SURRENDER;
		this.petObjectId = petObjectId;
	}

	/**
	 * For listing all pets on this character
	 */
	public SM_PET(Collection<PetCommonData> pets) {
		this.action = PetAction.LOAD_PETS;
		this.pets = pets;
	}

	public SM_PET(PetSpecialFunction specialFunction, boolean active) {
		this(specialFunction, active, 0);
	}

	public SM_PET(PetSpecialFunction specialFunction, boolean active, int npcObjId) {
		this.action = PetAction.SPECIAL_FUNCTION;
		this.isActing = active;
		this.subType = specialFunction.getId();
		this.lootNpcObjId = npcObjId;
	}

	public SM_PET(int dopeAction, int itemId, int slot) {
		this.action = PetAction.SPECIAL_FUNCTION;
		this.dopeAction = dopeAction;
		this.subType = PetSpecialFunction.DOPING.getId();
		itemObjectId = itemId; // it's template ID, not objectId though. also it's misused as slot2 for slot switch action (dopeAction 2)
		dopeSlot = slot;
	}

	/**
	 * For mood only
	 * 
	 * @param actionId
	 * @param pet
	 * @param shuggleEmotion
	 */
	public SM_PET(Pet pet, int subType, int shuggleEmotion) {
		this.action = PetAction.MOOD;
		this.shuggleEmotion = shuggleEmotion;
		this.subType = subType;
		this.commonData = pet.getCommonData();
	}

	/**
	 * For deleting pet visually.
	 */
	public SM_PET(int petObjectId, ObjectDeleteAnimation animation) {
		this.action = PetAction.DISMISS;
		this.petObjectId = petObjectId;
		this.animationId = animation.getId();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(action.getActionId());
		switch (action) {
			case LOAD_PETS: // load list on login
				writeC(0); // unk
				writeH(pets.size());
				for (PetCommonData commonData : pets) {
					writePetData(commonData);
				}
				break;
			case ADOPT:
				writePetData(commonData);
				break;
			case SURRENDER:
				writeD(commonData.getTemplateId());
				writeD(commonData.getObjectId());
				writeD(0); // unk
				writeD(0); // unk
				break;
			case SPAWN:
				writeS(pet.getName());
				writeD(pet.getObjectTemplate().getTemplateId());
				writeD(pet.getObjectId());

				writeF(pet.getPosition().getX());
				writeF(pet.getPosition().getY());
				writeF(pet.getPosition().getZ());
				writeF(pet.getMoveController().getTargetX2());
				writeF(pet.getMoveController().getTargetY2());
				writeF(pet.getMoveController().getTargetZ2());
				writeC(pet.getHeading());

				writeD(pet.getMaster().getObjectId());

				writeAppearance(pet.getCommonData());
				break;
			case DISMISS:
				writeD(petObjectId);
				writeC(animationId);
				break;
			case FOOD:
				writeH(1);
				writeC(1);
				writeC(subType);
				switch (subType) {
					case 1: // eat
						writeD(commonData.getFeedProgress().getDataForPacket());
						writeD(0);
						writeD(itemObjectId);
						writeD(count);
						break;
					case 2: // eating successful
						writeD(commonData.getFeedProgress().getDataForPacket());
						writeD(0);
						writeD(itemObjectId);
						writeD(count);
						writeC(0);
						break;
					case 3: // not hungry
					case 4: // cancel feed
					case 5: // clean feed task
						writeD(commonData.getFeedProgress().getDataForPacket());
						writeD((int) commonData.getRefeedDelay() / 1000);
						break;
					case 6: // give item
						writeD(commonData.getFeedProgress().getDataForPacket());
						writeD(0);
						writeD(itemObjectId);
						writeC(0);
						break;
					case 7: // present notification
						writeD(commonData.getFeedProgress().getDataForPacket());
						writeD((int) commonData.getRefeedDelay() / 1000); // time
						writeD(itemObjectId);
						writeD(0);
						break;
					case 8: // is full
						writeD(commonData.getFeedProgress().getDataForPacket());
						writeD((int) commonData.getRefeedDelay() / 1000);
						writeD(itemObjectId);
						writeD(count);
						break;
				}
				break;
			case RENAME:
				writeD(petObjectId);
				writeS(petName);
				break;
			case MOOD:
				switch (subType) {
					case 0: // check pet status
						writeC(subType);
						// desynced feedback data, need to send delta in percents
						if (commonData.getLastSentPoints() < commonData.getMoodPoints(true))
							writeD(commonData.getMoodPoints(true) - commonData.getLastSentPoints());
						else {
							writeD(0);
							commonData.setLastSentPoints(commonData.getMoodPoints(true));
						}
						break;
					case 2: // emotion sent
						writeC(subType);
						writeD(0);
						writeD(commonData.getMoodPoints(true));
						writeD(shuggleEmotion);
						commonData.setLastSentPoints(commonData.getMoodPoints(true));
						commonData.setMoodCdStarted(System.currentTimeMillis());
						break;
					case 3: // give gift
						writeC(subType);
						writeD(DataManager.PET_DATA.getPetTemplate(commonData.getTemplateId()).getConditionReward());
						commonData.setGiftCdStarted(System.currentTimeMillis());
						break;
					case 4: // periodic update
						writeC(subType);
						writeD(commonData.getMoodPoints(true));
						writeD(commonData.getMoodRemainingTime());
						writeD(commonData.getGiftRemainingTime());
						commonData.setLastSentPoints(commonData.getMoodPoints(true));
						break;
				}
				break;
			case SPECIAL_FUNCTION:
				writeC(subType);
				if (subType == 2) {
					writeC(dopeAction);
					switch (dopeAction) {
						case 0: // add item
							writeD(itemObjectId);
							writeD(dopeSlot);
							break;
						case 1: // remove item
							writeD(dopeSlot);
							break;
						case 2: // move item from one slot to other
							writeD(dopeSlot); // slot 1
							writeD(itemObjectId); // slot 2
							break;
						case 3: // use item
							writeD(itemObjectId);
							break;
					}
				} else if (subType == 3) {
					// looting NPC
					if (lootNpcObjId > 0) {
						writeC(isActing ? 1 : 2); // 0x02 display looted msg.
						writeD(lootNpcObjId);
					} else {
						// loot function activation
						writeC(0);
						writeC(isActing ? 1 : 0);
					}
				} else if (subType == 4) {
					writeC(0);
					writeC(isActing ? 1 : 0);
				}
				break;
		}
	}

	private void writePetData(PetCommonData petCommonData) {
		PetTemplate petTemplate = DataManager.PET_DATA.getPetTemplate(petCommonData.getTemplateId());
		writeS(petCommonData.getName());
		writeD(petCommonData.getTemplateId());
		writeD(petCommonData.getObjectId());
		writeD(petCommonData.getMasterObjectId());
		writeD(0);
		writeD(0);
		writeD(petCommonData.getBirthday());
		writeD(petCommonData.secondsUntilExpiration()); // accompanying time

		int specialtyCount = 0;
		if (petTemplate.containsFunction(PetFunctionType.WAREHOUSE)) {
			writeC(PetFunctionType.WAREHOUSE.getId());
			writeC(0); // length of following bytes
			specialtyCount++;
		}
		if (petTemplate.containsFunction(PetFunctionType.LOOT)) {
			writeC(PetFunctionType.LOOT.getId());
			writeC(1); // length of following bytes
			writeC(0);
			specialtyCount++;
		}
		if (petTemplate.containsFunction(PetFunctionType.DOPING)) {
			writeC(PetFunctionType.DOPING.getId());
			writeC(PetDopingBag.MAX_ITEMS * 4); // length of following bytes (always write MAX_ITEMS, otherwise some pets show items of other pets) 
			int[] items = petCommonData.getDopingBag().getItems();
			for (int i = 0; i < PetDopingBag.MAX_ITEMS; i++)
				writeD(i < items.length ? items[i] : 0);
			specialtyCount++;
		}
		if (petTemplate.containsFunction(PetFunctionType.FOOD)) {
			writeC(PetFunctionType.FOOD.getId());
			writeC(8); // length of following bytes
			writeD(petCommonData.getFeedProgress().getDataForPacket());
			writeD((int) (petCommonData.getRefeedDelay() / 1000));
			specialtyCount++;
		}

		// Pets have only 2 functions max. If absent filled with NONE
		if (specialtyCount == 0) {
			writeH(PetFunctionType.NONE.getId());
			writeH(PetFunctionType.NONE.getId());
		} else if (specialtyCount == 1) {
			writeH(PetFunctionType.NONE.getId());
		}

		writeAppearance(petCommonData);
	}

	private void writeAppearance(PetCommonData petCommonData) {
		writeH(PetFunctionType.APPEARANCE.getId());
		writeC(0); // not implemented color R ?
		writeC(0); // not implemented color G ?
		writeC(0); // not implemented color B ?
		writeD(petCommonData.getDecoration());
		writeD(0); // wings ID if customize_attach = 1
		writeD(0); // unk
	}
}
