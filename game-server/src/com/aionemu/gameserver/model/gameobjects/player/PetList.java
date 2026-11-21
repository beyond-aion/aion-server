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

	private long renameCooldownEndTime;
	private long lastSummonTime;
	private Map<Integer, PetCommonData> pets = new LinkedHashMap<>();

	PetList(Player player) {
		loadPets(player);
	}

	public void loadPets(Player player) {
		List<PetCommonData> playerPets = PlayerPetsDAO.getPlayerPets(player);
		for (PetCommonData pet : playerPets) {
			ExpireTimerTask.getInstance().registerExpirable(pet, player);
			pets.put(pet.getTemplateId(), pet); // the client only sends template ids for spawn/dismiss, so we cannot support multiple same pets
		}
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

	public PetCommonData addPet(Player player, int petId, int decorationId, long birthday, String name, int expireTime) {
		return addPet(player, petId, decorationId, 0, 0, 0, birthday, name, expireTime);
	}

	public PetCommonData addPet(Player player, int petId, int decorationId, int color, int face, int head, String name, int expireTime) {
		return addPet(player, petId, decorationId, color, face, head, System.currentTimeMillis(), name, expireTime);
	}

	public PetCommonData addPet(Player player, int petId, int decorationId, int color, int face, int head, long birthday, String name,
		int expireTime) {
		PetCommonData petCommonData = new PetCommonData(IDFactory.getInstance().nextId(), petId, player.getObjectId(), expireTime);
		petCommonData.setDecoration(decorationId);
		petCommonData.setColor(color);
		petCommonData.setFace(face);
		petCommonData.setHead(head);
		petCommonData.setName(name);
		petCommonData.setBirthday(new Timestamp(birthday));
		PlayerPetsDAO.insertPlayerPet(player, petCommonData);
		pets.put(petId, petCommonData);
		return petCommonData;
	}

	public boolean hasPet(int templateId) {
		return pets.containsKey(templateId);
	}

	public boolean hasSameName(String name) {
		return pets.values().stream().anyMatch(pet -> name.equals(pet.getName()));
	}

	public long getLastSummonTime() {
		return lastSummonTime;
	}

	public void setLastSummonTime(long lastSummonTime) {
		this.lastSummonTime = lastSummonTime;
	}

	public long getRenameCooldownEndTime() {
		return renameCooldownEndTime;
	}

	public void setRenameCooldownEndTime(long renameCooldownEndTime) {
		this.renameCooldownEndTime = renameCooldownEndTime;
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
