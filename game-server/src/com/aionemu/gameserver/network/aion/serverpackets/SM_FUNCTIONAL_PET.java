package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetAction;
import com.aionemu.gameserver.model.gameobjects.PetDopingAction;
import com.aionemu.gameserver.model.gameobjects.PetFeedAction;
import com.aionemu.gameserver.model.gameobjects.PetMoodAction;
import com.aionemu.gameserver.model.gameobjects.PetSpecialFunction;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.templates.pet.PetDopingBag;
import com.aionemu.gameserver.model.templates.pet.PetFunction;
import com.aionemu.gameserver.model.templates.pet.PetFunctionType;
import com.aionemu.gameserver.model.templates.pet.PetTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author M@xx, xTz, Rolandas
 */
public class SM_FUNCTIONAL_PET extends AionServerPacket {

	public static final int MAX_ENTRIES_PER_PAGE = 50;
	private static final int FEEDING_FUNCTION_LENGTH = 2 * Integer.BYTES; // feed data and refeed time

	private PetAction action;
	private Pet pet;
	private int petObjectId;
	private PetCommonData commonData;
	private String petName;
	private int itemObjectId;
	private Collection<PetCommonData> pets;
	private int count;
	private int page;
	private PetSpecialFunction specialFunction;
	private PetFeedAction feedAction;
	private PetMoodAction moodAction;
	private int shuggleEmotion;
	private boolean isActing;
	private int lootNpcObjId;
	private PetDopingAction dopeAction;
	private int dopeSlot;
	private boolean dopingFailed;
	private int result;
	private int remainingSeconds;

	private SM_FUNCTIONAL_PET(PetAction action) {
		this.action = action;
	}

	/**
	 * Pet list on login. The client resets its list on page 0 and appends the following pages.
	 */
	public static SM_FUNCTIONAL_PET petList(Collection<PetCommonData> pets, int page) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.LOAD_PETS);
		packet.pets = pets;
		packet.page = page;
		return packet;
	}

	/**
	 * A newly adopted pet; the client adds it to the list.
	 */
	public static SM_FUNCTIONAL_PET adopted(PetCommonData pet) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.ADOPT);
		packet.commonData = pet;
		return packet;
	}

	/**
	 * A pet the player gave away.
	 */
	public static SM_FUNCTIONAL_PET surrendered(PetCommonData pet) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.SURRENDER);
		packet.commonData = pet;
		return packet;
	}

	/**
	 * A pet whose rental time ran out; the client shows the "time is up" message itself.
	 */
	public static SM_FUNCTIONAL_PET abandoned(PetCommonData pet) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.ABANDON);
		packet.commonData = pet;
		return packet;
	}

	/**
	 * Remaining lifetime after the pet's rental time was extended by an item.
	 */
	public static SM_FUNCTIONAL_PET extended(PetCommonData pet, int remainingSeconds) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.EXTEND_EXPIRATION);
		packet.commonData = pet;
		packet.remainingSeconds = remainingSeconds;
		return packet;
	}

	/**
	 * Appearance and position of a pet that enters the world.
	 */
	public static SM_FUNCTIONAL_PET spawned(Pet pet) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.SPAWN);
		packet.pet = pet;
		packet.commonData = pet.getCommonData();
		return packet;
	}

	/**
	 * A pet that leaves the world; the client only plays the fade out animation.
	 */
	public static SM_FUNCTIONAL_PET dismissed(int petObjectId) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.DISMISS);
		packet.petObjectId = petObjectId;
		return packet;
	}

	public static SM_FUNCTIONAL_PET feeding(PetFeedAction action, int itemObjectId, int count, Pet pet) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.FOOD);
		packet.feedAction = action;
		packet.itemObjectId = itemObjectId;
		packet.count = count;
		packet.commonData = pet.getCommonData();
		return packet;
	}

	public static SM_FUNCTIONAL_PET renamed(int petObjectId, String name) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.RENAME);
		packet.petObjectId = petObjectId;
		packet.petName = name;
		return packet;
	}

	/**
	 * Result of a pet adoption or extension attempt (0 = success is not sent, 3 = invalid name, 4 = name already used).
	 */
	public static SM_FUNCTIONAL_PET adoptionResult(int result) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.RESULT);
		packet.result = result;
		return packet;
	}

	public static SM_FUNCTIONAL_PET mood(Pet pet, PetMoodAction action, int shuggleEmotion) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.MOOD);
		packet.moodAction = action;
		packet.shuggleEmotion = shuggleEmotion;
		packet.commonData = pet.getCommonData();
		return packet;
	}

	public static SM_FUNCTIONAL_PET specialFunction(PetSpecialFunction function, boolean active) {
		return specialFunction(function, active, 0);
	}

	public static SM_FUNCTIONAL_PET specialFunction(PetSpecialFunction function, boolean active, int npcObjId) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.SPECIAL_FUNCTION);
		packet.specialFunction = function;
		packet.isActing = active;
		packet.lootNpcObjId = npcObjId;
		return packet;
	}

	public static SM_FUNCTIONAL_PET doping(PetDopingAction action, int itemId, int slot) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.SPECIAL_FUNCTION);
		packet.specialFunction = PetSpecialFunction.DOPING;
		packet.dopeAction = action;
		packet.itemObjectId = itemId; // template id, or the second slot for the switch action
		packet.dopeSlot = slot;
		return packet;
	}

	/**
	 * Rejection of a doping action; the client rolls its optimistic UI change back.
	 */
	public static SM_FUNCTIONAL_PET dopingRejected(PetDopingAction action) {
		SM_FUNCTIONAL_PET packet = new SM_FUNCTIONAL_PET(PetAction.SPECIAL_FUNCTION);
		packet.specialFunction = PetSpecialFunction.DOPING;
		packet.dopeAction = action;
		packet.dopingFailed = true;
		return packet;
	}

	public static SM_FUNCTIONAL_PET petAdoptDialog() {
		return new SM_FUNCTIONAL_PET(PetAction.TALK_WITH_MERCHANT);
	}

	public static SM_FUNCTIONAL_PET petAbandonDialog() {
		return new SM_FUNCTIONAL_PET(PetAction.TALK_WITH_MINDER);
	}

	public static SM_FUNCTIONAL_PET petAdoptHyperlink() {
		return new SM_FUNCTIONAL_PET(PetAction.H_ADOPT);
	}

	public static SM_FUNCTIONAL_PET petAbandonHyperlink() {
		return new SM_FUNCTIONAL_PET(PetAction.H_ABANDON);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(action.getActionId());
		switch (action) {
			case LOAD_PETS: // load list on login, several pages if the player owns many pets
				writeC(page);
				writeH(pets.size());
				for (PetCommonData commonData : pets)
					writePetEntry(commonData);
				break;
			case ADOPT:
				writePetEntry(commonData);
				break;
			case SURRENDER: // forms 2 and 14 share their body
			case ABANDON:
				writeD(commonData.getTemplateId());
				writeD(commonData.getObjectId());
				writeQ(0); // unused by the client, always 0
				break;
			case SPAWN:
				writeS(pet.getName());
				writeD(pet.getObjectTemplate().getTemplateId());
				writeD(pet.getObjectId());
				writeF(pet.getX());
				writeF(pet.getY());
				writeF(pet.getZ());
				// the pet is spawned at its master's position, so the target is the position
				writeF(pet.getX());
				writeF(pet.getY());
				writeF(pet.getZ());
				writeC(pet.getMaster().getHeading());
				writeD(pet.getMaster().getObjectId());
				writeAppearance(commonData);
				break;
			case DISMISS:
				writeD(petObjectId);
				writeC(1); // fade out
				break;
			case FOOD:
				writeH(1); // one function block follows
				writeC(1); // feeding function tag
				writeC(feedAction.getId());
				writeD(commonData.getFeedProgress().getDataForPacket());
				writeD((int) commonData.getRefeedDelay() / 1000);
				switch (feedAction) {
					case START: // start feeding
					case REFEED: // refeed timer started
					case REJECTED: // food rejected or not hungry
						writeD(itemObjectId);
						writeD(count);
						break;
					case EATEN: // one item was eaten
						writeD(itemObjectId);
						writeD(count);
						writeC(1); // the food matched the pet's flavour
						break;
					case STOPPED: // feeding stopped by the server
						writeC(0);
						break;
					case GAVE_ITEM: // the pet gave an item back
						writeD(itemObjectId);
						writeC(1); // the food matched the pet's flavour
						break;
					default: // CANCEL, FINISHED: no extra data
						break;
				}
				break;
			case RENAME:
				writeD(petObjectId);
				writeS(petName);
				break;
			case RESULT:
				writeC(result);
				break;
			case MOOD:
				writeC(moodAction.getId());
				switch (moodAction) {
					case STATUS: // the client plays one of three idle animations
						writeD(Rnd.get(0, 2));
						break;
					case SOCIAL:
						writeD(Rnd.get(0, 8));
						writeD(commonData.getMoodPoints(true));
						writeD(shuggleEmotion);
						break;
					case PRESENT:
						writeD(DataManager.PET_DATA.getPetTemplate(commonData.getTemplateId()).getConditionReward());
						break;
					case UPDATE:
						writeD(commonData.getMoodPoints(true));
						writeD(commonData.getMoodRemainingTime());
						writeD(commonData.getGiftRemainingTime());
						break;
				}
				break;
			case SPECIAL_FUNCTION:
				writeC(specialFunction.getId());
				if (specialFunction == PetSpecialFunction.DOPING) {
					writeC(dopeAction.getId());
					if (dopingFailed) {
						switch (dopeAction) {
							case ADD:
							case SWITCH:
								// the client reads both parameters for these actions; -1 is the failure sentinel
								writeD(-1);
								writeD(-1);
								break;
							default: // REMOVE, USE
								writeD(-1);
								break;
						}
					} else {
						switch (dopeAction) {
							case ADD:
								writeD(itemObjectId);
								writeD(dopeSlot);
								break;
							case REMOVE:
								writeD(dopeSlot);
								break;
							case SWITCH:
								writeD(dopeSlot); // slot 1
								writeD(itemObjectId); // slot 2
								break;
							case USE:
								writeD(itemObjectId);
								break;
						}
					}
				} else if (specialFunction == PetSpecialFunction.AUTOLOOT) {
					if (lootNpcObjId > 0) {
						writeC(isActing ? 1 : 2); // 0x02 display looted msg.
						writeD(lootNpcObjId);
					} else {
						// loot function activation
						writeC(0);
						writeC(isActing ? 1 : 0);
					}
				} else if (specialFunction == PetSpecialFunction.AUTOSELL) {
					writeC(0);
					writeC(isActing ? 1 : 0);
				}
				break;
			case EXTEND_EXPIRATION:
				writeD(commonData.getTemplateId());
				writeD(commonData.getObjectId());
				writeD(remainingSeconds);
				break;
			case TALK_WITH_MERCHANT:
			case TALK_WITH_MINDER:
			case H_ADOPT:
			case H_ABANDON:
				// actions without a body
				break;
			default:
				break;
		}
	}

	private void writePetEntry(PetCommonData petCommonData) {
		PetTemplate petTemplate = DataManager.PET_DATA.getPetTemplate(petCommonData.getTemplateId());
		writeS(petCommonData.getName());
		writeD(petCommonData.getTemplateId());
		writeD(petCommonData.getObjectId());
		writeD(petCommonData.getMasterObjectId());
		writeD(0); // always 0
		writeD(0); // always 0
		writeD(petCommonData.getBirthday());
		int expiration = petCommonData.secondsUntilExpiration();
		writeD(Math.max(expiration, 0)); // 0 for pets without (or past) expiration

		// a pet has at most two player functions, written in template order; missing slots are empty
		int writtenFunctions = 0;
		for (PetFunction function : petTemplate.getPetFunctions()) {
			if (writtenFunctions == 2)
				break;
			PetFunctionType type = function.getPetFunctionType();
			if (!type.isPlayerFunction() || type == PetFunctionType.NONE)
				continue;
			writeFunction(type, petCommonData);
			writtenFunctions++;
		}
		for (; writtenFunctions < 2; writtenFunctions++)
			writeFunction(PetFunctionType.WAREHOUSE, null); // empty slot: type 0 with no data
		writeAppearance(petCommonData);
	}

	private void writeFunction(PetFunctionType type, PetCommonData petCommonData) {
		writeC(type.getId());
		switch (type) {
			case LOOT:
				writeC(1);
				writeC(petCommonData.isLooting() ? 1 : 0);
				break;
			case DOPING:
				writeC(PetDopingBag.MAX_ITEMS * Integer.BYTES);
				int[] items = petCommonData.getDopingBag().getItems();
				for (int i = 0; i < PetDopingBag.MAX_ITEMS; i++)
					writeD(i < items.length ? items[i] : 0);
				break;
			case FOOD:
				writeC(FEEDING_FUNCTION_LENGTH);
				writeD(petCommonData.getFeedProgress().getDataForPacket());
				writeD((int) (petCommonData.getRefeedDelay() / 1000));
				break;
			default: // warehouse, merchant and buff carry no data
				writeC(0);
				break;
		}
	}

	/**
	 * The 17 byte appearance block: block type, color, decoration and the two attachment item ids, as sent by the client at
	 * adoption.
	 */
	private void writeAppearance(PetCommonData petCommonData) {
		writeC(PetFunctionType.APPEARANCE.getId()); // block type
		writeD(petCommonData.getColor());
		writeD(petCommonData.getDecoration());
		writeD(petCommonData.getFace());
		writeD(petCommonData.getHead());
	}
}
