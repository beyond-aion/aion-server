package com.aionemu.gameserver.model.gameobjects.player;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.PlayerPetsDAO;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;

/**
 * @author ATracer
 */
public class PetList {

	private final Player player;
	private int lastUsedPetId;
	private LinkedHashMap<Integer, PetCommonData> pets = new LinkedHashMap<>();

	PetList(Player player) {
		this.player = player;
		loadPets();
	}

	public void loadPets() {
		List<PetCommonData> playerPets = DAOManager.getDAO(PlayerPetsDAO.class).getPlayerPets(player);
		PetCommonData lastUsedPet = null;
		for (PetCommonData pet : playerPets) {
			ExpireTimerTask.getInstance().registerExpirable(pet, player);
			pets.put(pet.getPetId(), pet);
			if (lastUsedPet == null || pet.getDespawnTime().after(lastUsedPet.getDespawnTime()))
				lastUsedPet = pet;
		}

		if (lastUsedPet != null)
			lastUsedPetId = lastUsedPet.getPetId();
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
		return getPet(lastUsedPetId);
	}

	public void setLastUsedPetId(int lastUsedPetId) {
		this.lastUsedPetId = lastUsedPetId;
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
		PetCommonData petCommonData = new PetCommonData(petId, player.getObjectId(), expireTime);
		petCommonData.setDecoration(decorationId);
		petCommonData.setName(name);
		petCommonData.setBirthday(new Timestamp(birthday));
		petCommonData.setDespawnTime(new Timestamp(System.currentTimeMillis()));
		DAOManager.getDAO(PlayerPetsDAO.class).insertPlayerPet(petCommonData);
		pets.put(petId, petCommonData);
		return petCommonData;
	}

	/**
	 * @param petId
	 * @return
	 */
	public boolean hasPet(int petId) {
		return pets.containsKey(petId);
	}

	/**
	 * @param petId
	 */
	public void deletePet(int petId) {
		if (hasPet(petId)) {
			pets.remove(petId);
			DAOManager.getDAO(PlayerPetsDAO.class).removePlayerPet(player, petId);
		}
	}
}
