package com.aionemu.gameserver.model.gameobjects.player;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.dao.PlayerPetsDAO;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public class PetList {

	private int lastUsedPetTemplateId;
	private Map<Integer, PetCommonData> pets = new LinkedHashMap<>();

	PetList(Player player) {
		loadPets(player);
	}

	public void loadPets(Player player) {
		List<PetCommonData> playerPets = PlayerPetsDAO.getPlayerPets(player);
		PetCommonData lastUsedPet = null;
		for (PetCommonData pet : playerPets) {
			ExpireTimerTask.getInstance().registerExpirable(pet, player);
			pets.put(pet.getTemplateId(), pet); // the client only sends template ids for spawn/dismiss, so we cannot support multiple same pets
			if (lastUsedPet == null || pet.getDespawnTime().after(lastUsedPet.getDespawnTime()))
				lastUsedPet = pet;
		}

		if (lastUsedPet != null)
			lastUsedPetTemplateId = lastUsedPet.getObjectId();
	}

	public Collection<PetCommonData> getPets() {
		return pets.values();
	}

	/**
	 * @param petId
	 * @return
	 */
	public PetCommonData getPet(int petId) {
		return pets.get(petId);
	}

	public PetCommonData getLastUsedPet() {
		return getPet(lastUsedPetTemplateId);
	}

	public void setLastUsedPetTemplateId(int lastUsedPetTemplateId) {
		this.lastUsedPetTemplateId = lastUsedPetTemplateId;
	}

	/**
	 * @param player
	 * @param petId
	 * @param decorationId
	 * @param name
	 * @return
	 */
	public PetCommonData addPet(Player player, int petId, int decorationId, String name, int expireTime) {
		return addPet(player, petId, decorationId, System.currentTimeMillis(), name, expireTime);
	}

	public PetCommonData addPet(Player player, int petId, int decorationId, long birthday, String name, int expireTime) {
		PetCommonData petCommonData = new PetCommonData(IDFactory.getInstance().nextId(), petId, player.getObjectId(), expireTime);
		petCommonData.setDecoration(decorationId);
		petCommonData.setName(name);
		petCommonData.setBirthday(new Timestamp(birthday));
		petCommonData.setDespawnTime(new Timestamp(System.currentTimeMillis()));
		PlayerPetsDAO.insertPlayerPet(player, petCommonData);
		pets.put(petId, petCommonData);
		return petCommonData;
	}

	public boolean hasPet(int templateId) {
		return pets.containsKey(templateId);
	}

	public PetCommonData deletePet(int templateId) {
		PetCommonData petCommonData = pets.remove(templateId);
		if (petCommonData != null) {
			PlayerPetsDAO.removePlayerPet(petCommonData.getObjectId());
			return petCommonData;
		}
		return null;
	}
}
