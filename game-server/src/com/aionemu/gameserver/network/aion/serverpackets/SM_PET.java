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
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author SVDNESS
 * @version 4.8 [JDK 25]
 */

//Фул новый пакет.
public class SM_PET extends AionServerPacket {
	private final PetAction action;
	private Pet pet;
	private int petObjectId;
	private PetCommonData commonData;
	private String petName;
	private int itemObjectId;
	private Collection<PetCommonData> pets;
	private int count;
	private int subType;
	private int snuggleEmotion;
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

	@SuppressWarnings("unused")
	public SM_PET(int petId, int petObjectId) {
		this.action = PetAction.SURRENDER;
		this.petObjectId = petObjectId;
	}

	public SM_PET(Collection<PetCommonData> pets) {
		this.action = PetAction.LOAD_PETS;
		this.pets = pets;
	}

	public SM_PET(boolean isBuffing) {
		this.action = PetAction.SPECIAL_FUNCTION;
		this.isActing = isBuffing;
		this.subType = 5;
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
		itemObjectId = itemId;
		dopeSlot = slot;
	}

	public SM_PET(Pet pet, int subType, int snuggleEmotion) {
		this.action = PetAction.MOOD;
		this.snuggleEmotion = snuggleEmotion;
		this.subType = subType;
		this.commonData = pet.getCommonData();
	}

	public SM_PET(int petObjectId, ObjectDeleteAnimation animation) {
		this.action = PetAction.DISMISS;
		this.petObjectId = petObjectId;
		this.animationId = animation.getId();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(action.getActionId());
		switch (action) {
			case LOAD_PETS -> { //Загрузка листа питомцев при входе в игру.
				writeC(0);
				writeH(pets.size());
				for (var commonData : pets) {
					writePetData(commonData);
				}
			}
			case ADOPT -> writePetData(commonData); //Взять питомца.
			case SURRENDER -> { //Далеко от владельца.
				writeD(commonData.getTemplateId());
				writeD(commonData.getObjectId());
				writeD(0);
				writeD(0);
			}
			case SPAWN -> { //Призыв питомца.
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
			}
			case DISMISS -> { //Отмена призыва питомца.
				writeD(petObjectId);
				writeC(animationId);
			}
			case FOOD -> { //Кормление.
				writeH(1);
				writeC(1);
				writeC(subType);
				switch (subType) {
					case 1 -> { //Кормление.
						writeD(commonData.getFeedProgress().getDataForPacket());
						writeD(0);
						writeD(itemObjectId);
						writeD(count);
					}
					case 2 -> { //Успешное кормление.
						writeD(commonData.getFeedProgress().getDataForPacket());
						writeD(0);
						writeD(itemObjectId);
						writeD(count);
						writeC(0);
					}
					case 3, 4, 5 -> { //Не голоден| Отмена кормления | Очистки задачи кормления.
						writeD(commonData.getFeedProgress().getDataForPacket());
						writeD((int) commonData.getRefeedDelay() / 1000);
					}
					case 6 -> { //Дать предмет.
						writeD(commonData.getFeedProgress().getDataForPacket());
						writeD(0);
						writeD(itemObjectId);
						writeC(0);
					}
					case 7 -> { //Текущее уведомление.
						writeD(commonData.getFeedProgress().getDataForPacket());
						writeD((int) commonData.getRefeedDelay() / 1000); // time
						writeD(itemObjectId);
						writeD(0);
					}
					case 8 -> { //Наелся.
						writeD(commonData.getFeedProgress().getDataForPacket());
						writeD((int) commonData.getRefeedDelay() / 1000);
						writeD(itemObjectId);
						writeD(count);
					}
				}
			}
			case RENAME -> { //Смена имени питомца.
				writeD(petObjectId);
				writeS(petName);
			}
			case MOOD -> { //Настроение.
				switch (subType) {
					case 0 -> { //Проверка статуса питомца.
						writeC(subType);
						if (commonData.getLastSentPoints() < commonData.getMoodPoints(true)) {
							writeD(commonData.getMoodPoints(true) - commonData.getLastSentPoints());
						} else {
							writeD(0);
							commonData.setLastSentPoints(commonData.getMoodPoints(true));
						}
					}
					case 2 -> { //Отправленная эмоция питомцем.
						writeC(subType);
						writeD(0);
						writeD(commonData.getMoodPoints(true));
						writeD(snuggleEmotion);
						commonData.setLastSentPoints(commonData.getMoodPoints(true));
						commonData.setMoodCdStarted(System.currentTimeMillis());
					}
					case 3 -> { //Дать подарок.
						writeC(subType);
						writeD(DataManager.PET_DATA.getPetTemplate(commonData.getTemplateId()).getConditionReward());
						commonData.setGiftCdStarted(System.currentTimeMillis());
					}
					case 4 -> { //Периодическое обновление.
						writeC(subType);
						writeD(commonData.getMoodPoints(true));
						writeD(commonData.getMoodRemainingTime());
						writeD(commonData.getGiftRemainingTime());
						commonData.setLastSentPoints(commonData.getMoodPoints(true));
					}
				}
			}
			case SPECIAL_FUNCTION -> { //Специальные функции.
				writeC(subType);
				if (subType == 2) {
					writeC(dopeAction);
					switch (dopeAction) {
						case 0 -> { //Добавление предмет.
							writeD(itemObjectId);
							writeD(dopeSlot);
						}
						case 1 -> writeD(dopeSlot); //Удаление предмет.
						case 2 -> {
							writeD(dopeSlot); //Слот 1.
							writeD(itemObjectId); //Слот 2.
						}
						case 3 -> writeD(itemObjectId); //Использование предмета.
					}
				} else if (subType == 3) {
					if (lootNpcObjId > 0) {
						writeC(isActing ? 1 : 2); //Сообщение на экран об успешном луте.
						writeD(lootNpcObjId);
					} else {
						writeC(0);
						writeC(isActing ? 1 : 0);
					}
				} else if (subType == 4) {
					writeC(0);
					writeC(isActing ? 1 : 0);
				} else if (subType == 5) {
					writeC(isActing ? 0 : 1);
				}
			}
		}
	}

	private void writePetData(PetCommonData petCommonData) {
		final var petTemplate = DataManager.PET_DATA.getPetTemplate(petCommonData.getTemplateId());
		writeS(petCommonData.getName());
		writeD(petCommonData.getTemplateId());
		writeD(petCommonData.getObjectId());
		writeD(petCommonData.getMasterObjectId());
		writeD(0);
		writeD(0);
		writeD(petCommonData.getBirthday());
		writeD(petCommonData.secondsUntilExpiration());
		int specialtyCount = 0;
		if (petTemplate.containsFunction(PetFunctionType.WAREHOUSE)) {
			writeC(PetFunctionType.WAREHOUSE.getId());
			writeC(0);
			specialtyCount++;
		}
		if (petTemplate.containsFunction(PetFunctionType.LOOT)) {
			writeC(PetFunctionType.LOOT.getId());
			writeC(1);
			writeC(0);
			specialtyCount++;
		}
		//Функция "Помощь" с использованием Эфирной вишенки.
		if (petTemplate.containsFunction(PetFunctionType.CHERRY)) {
			writeC(PetFunctionType.CHERRY.getId());
			writeC(2);
			short cheerId = (short) petTemplate.getPetFunction(PetFunctionType.CHERRY).getId();
			writeH(cheerId);
			specialtyCount++;
		}
		if (petTemplate.containsFunction(PetFunctionType.DOPING)) {
			writeC(PetFunctionType.DOPING.getId());
			writeC(PetDopingBag.MAX_ITEMS * 4);
			int[] items = petCommonData.getDopingBag().getItems();
			for (int i = 0; i < PetDopingBag.MAX_ITEMS; i++) {
				writeD(i < items.length ? items[i] : 0);
			}
			specialtyCount++;
		}
		if (petTemplate.containsFunction(PetFunctionType.FOOD)) {
			writeC(PetFunctionType.FOOD.getId());
			writeC(8);
			writeD(petCommonData.getFeedProgress().getDataForPacket());
			writeD((int) (petCommonData.getRefeedDelay() / 1000));
			specialtyCount++;
		}
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
		writeC(0);
		writeC(0);
		writeC(0);
		writeD(petCommonData.getDecoration());
		writeD(0);
		writeD(0);
	}
}