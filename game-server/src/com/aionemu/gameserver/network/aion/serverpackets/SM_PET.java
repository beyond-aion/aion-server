package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetAction;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.templates.pet.PetDopingEntry;
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
	private PetCommonData commonData;
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

	public SM_PET(int subType, PetAction action, int itemObjectId, int count, Pet pet) {
		this.subType = subType;
		this.action = action;
		this.count = count;
		this.itemObjectId = itemObjectId;
		this.pet = pet;
		this.commonData = pet.getCommonData();
	}

	public SM_PET(PetAction action) {
		this.action = action;
	}

	public SM_PET(PetAction action, Pet pet) {
		this(0, action, 0, 0, pet);
	}

	/**
	 * For adopt only
	 * 
	 * @param actionId
	 * @param commonData
	 */
	public SM_PET(PetAction action, PetCommonData commonData) {
		this.action = action;
		this.commonData = commonData;
	}

	/**
	 * For listing all pets on this character
	 * 
	 * @param actionId
	 * @param pets
	 */
	public SM_PET(PetAction action, Collection<PetCommonData> pets) {
		this.action = action;
		this.pets = pets;
	}

	public SM_PET(boolean isLooting) {
		this(isLooting, 0);
	}

	public SM_PET(boolean isLooting, int npcObjId) {
		this.action = PetAction.SPECIAL_FUNCTION;
		this.isActing = isLooting;
		this.subType = 3;
		this.lootNpcObjId = npcObjId;
	}

	public SM_PET(int dopeAction, boolean isBuffing) {
		this.action = PetAction.SPECIAL_FUNCTION;
		this.dopeAction = dopeAction;
		this.isActing = isBuffing;
		this.subType = 2;
	}

	public SM_PET(int dopeAction, int itemId, int slot) {
		this(dopeAction, true);
		itemObjectId = itemId; // it's template ID, not objectId though
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
		this(0, PetAction.MOOD, 0, 0, pet);
		this.shuggleEmotion = shuggleEmotion;
		this.subType = subType;
	}

	/**
	 * For deleting pet visually.
	 */
	public SM_PET(Pet pet, ObjectDeleteAnimation animation) {
		this.pet = pet;
		this.action = PetAction.DISMISS;
		this.animationId = animation.getId();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		PetTemplate petTemplate = null;
		writeH(action.getActionId());
		switch (action) {
			case LOAD_PETS: // load list on login
				writeC(0); // unk
				writeH(pets.size());
				for (PetCommonData petCommonData : pets) {
					petTemplate = DataManager.PET_DATA.getPetTemplate(petCommonData.getPetId());
					int expireTime = petCommonData.getExpireTime();
					writeS(petCommonData.getName());
					writeD(petCommonData.getPetId());
					writeD(petCommonData.getObjectId());
					writeD(petCommonData.getMasterObjectId());
					writeD(0);
					writeD(0);
					writeD(petCommonData.getBirthday());
					writeD(expireTime != 0 ? expireTime - (int) (System.currentTimeMillis() / 1000) : 0); // accompanying time

					int specialtyCount = 0;
					if (petTemplate.containsFunction(PetFunctionType.WAREHOUSE)) {
						writeH(PetFunctionType.WAREHOUSE.getId());
						specialtyCount++;
					}
					if (petTemplate.containsFunction(PetFunctionType.LOOT)) {
						writeH(PetFunctionType.LOOT.getId());
						writeC(0);
						specialtyCount++;
					}
					if (petTemplate.containsFunction(PetFunctionType.DOPING)) {
						writeH(PetFunctionType.DOPING.getId());
						short dopeId = (short) petTemplate.getPetFunction(PetFunctionType.DOPING).getId();
						PetDopingEntry dope = DataManager.PET_DOPING_DATA.getDopingTemplate(dopeId);
						writeD(dope.isUseFood() ? petCommonData.getDopingBag().getFoodItem() : 0);
						writeD(dope.isUseDrink() ? petCommonData.getDopingBag().getDrinkItem() : 0);
						int[] scrollBag = petCommonData.getDopingBag().getScrollsUsed();
						if (scrollBag.length == 0) {
							writeQ(0);
							writeQ(0);
							writeQ(0);
						} else {
							writeD(scrollBag[0]); // Scroll 1
							writeD(scrollBag.length > 1 ? scrollBag[1] : 0); // Scroll 2
							writeD(scrollBag.length > 2 ? scrollBag[2] : 0); // Scroll 3 - no pet supports it yet
							writeD(scrollBag.length > 3 ? scrollBag[3] : 0); // Scroll 4 - no pet supports it yet
							writeD(scrollBag.length > 4 ? scrollBag[4] : 0); // Scroll 5 - no pet supports it yet
							writeD(scrollBag.length > 5 ? scrollBag[5] : 0); // Scroll 6 - no pet supports it yet
						}
						specialtyCount++;
					}
					if (petTemplate.containsFunction(PetFunctionType.FOOD)) {
						writeH(PetFunctionType.FOOD.getId());
						writeD(petCommonData.getFeedProgress().getDataForPacket());
						writeD((int) petCommonData.getRefeedDelay() / 1000);
						specialtyCount++;
					}

					// Pets have only 2 functions max. If absent filled with NONE
					if (specialtyCount == 0) {
						writeH(PetFunctionType.NONE.getId());
						writeH(PetFunctionType.NONE.getId());
					} else if (specialtyCount == 1) {
						writeH(PetFunctionType.NONE.getId());
					}

					writeH(PetFunctionType.APPEARANCE.getId());
					writeC(0); // not implemented color R ?
					writeC(0); // not implemented color G ?
					writeC(0); // not implemented color B ?
					writeD(petCommonData.getDecoration());

					// epilog
					writeD(0); // unk
					writeD(0); // unk
				}
				break;
			case ADOPT:
				writeS(commonData.getName());
				writeD(commonData.getPetId());
				writeD(commonData.getObjectId());
				writeD(commonData.getMasterObjectId());
				writeD(0);
				writeD(0);
				writeD(commonData.getBirthday());
				writeD(commonData.getExpireTime() != 0 ? commonData.getExpireTime() - (int) (System.currentTimeMillis() / 1000) : 0); // accompanying time
				petTemplate = DataManager.PET_DATA.getPetTemplate(commonData.getPetId());
				int specialtyCount = 0;
				if (petTemplate.containsFunction(PetFunctionType.WAREHOUSE)) {
					writeH(PetFunctionType.WAREHOUSE.getId());
					specialtyCount++;
				}
				if (petTemplate.containsFunction(PetFunctionType.LOOT)) {
					writeH(PetFunctionType.LOOT.getId());
					writeC(0);
					specialtyCount++;
				}
				if (petTemplate.containsFunction(PetFunctionType.DOPING)) {
					writeH(PetFunctionType.DOPING.getId());
					writeQ(0);
					writeQ(0);
					writeQ(0);
					writeQ(0);
					specialtyCount++;
				}
				if (petTemplate.containsFunction(PetFunctionType.FOOD)) {
					writeH(PetFunctionType.FOOD.getId());
					writeQ(0);
					specialtyCount++;
				}
				// Pets have only 2 functions max. If absent filled with NONE
				if (specialtyCount == 0) {
					writeH(PetFunctionType.NONE.getId());
					writeH(PetFunctionType.NONE.getId());
				} else if (specialtyCount == 1) {
					writeH(PetFunctionType.NONE.getId());
				}

				writeH(PetFunctionType.APPEARANCE.getId());
				writeC(0); // not implemented color R ?
				writeC(0); // not implemented color G ?
				writeC(0); // not implemented color B ?
				writeD(commonData.getDecoration());

				// epilog
				writeD(0); // unk
				writeD(0); // unk
				break;
			case SURRENDER:
				writeD(commonData.getPetId());
				writeD(commonData.getObjectId());
				writeD(0); // unk
				writeD(0); // unk
				break;
			case SPAWN:
				writeS(pet.getName());
				writeD(pet.getPetId());
				writeD(pet.getObjectId());

				writeF(pet.getPosition().getX());
				writeF(pet.getPosition().getY());
				writeF(pet.getPosition().getZ());
				writeF(pet.getMoveController().getTargetX2());
				writeF(pet.getMoveController().getTargetY2());
				writeF(pet.getMoveController().getTargetZ2());
				writeC(pet.getHeading());

				writeD(pet.getMaster().getObjectId());

				writeC(1); // unk
				writeD(0); // accompanying time ??
				writeD(pet.getCommonData().getDecoration());
				writeD(0); // wings ID if customize_attach = 1
				writeD(0); // unk
				break;
			case DISMISS:
				writeD(pet.getObjectId());
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
				writeD(pet.getObjectId());
				writeS(pet.getName());
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
						writeD(pet.getCommonData().getMoodPoints(true));
						writeD(shuggleEmotion);
						commonData.setLastSentPoints(pet.getCommonData().getMoodPoints(true));
						commonData.setMoodCdStarted(System.currentTimeMillis());
						break;
					case 3: // give gift
						writeC(subType);
						writeD(pet.getPetTemplate().getConditionReward());
						commonData.setGiftCdStarted(System.currentTimeMillis());
						break;
					case 4: // periodic update
						writeC(subType);
						writeD(commonData.getMoodPoints(true));
						writeD(commonData.getMoodRemainingTime());
						writeD(commonData.getGiftRemainingTime());
						commonData.setLastSentPoints(pet.getCommonData().getMoodPoints(true));
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
							writeD(0);
							break;
						case 2: // TODO: move item from one slot to other
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
				}
				break;
		}
	}
}
