package com.aionemu.gameserver.services.toypet;

import java.sql.Timestamp;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.PeriodicSaveConfig;
import com.aionemu.gameserver.controllers.PetController;
import com.aionemu.gameserver.dao.PlayerPetsDAO;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.pet.PetDopingBag;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer
 */
public class PetSpawnService {

	/**
	 * @param player
	 * @param petId
	 */
	public static final void summonPet(Player player, int petId) {
		PetCommonData lastPetCommonData;

		if (player.getPet() != null) {
			if (player.getPet().getPetId() == petId)
				return;
			lastPetCommonData = player.getPet().getCommonData();
			dismissPet(player);
		} else {
			lastPetCommonData = player.getPetList().getLastUsedPet();
		}

		if (lastPetCommonData != null && lastPetCommonData.getPetId() != petId) // reset mood if other pet is spawned
			lastPetCommonData.clearMoodStatistics();

		player.getController().addTask(TaskId.PET_UPDATE, ThreadPoolManager.getInstance().scheduleAtFixedRate(new PetController.PetUpdateTask(player),
			PeriodicSaveConfig.PLAYER_PETS * 1000, PeriodicSaveConfig.PLAYER_PETS * 1000));

		Pet pet = VisibleObjectSpawner.spawnPet(player, petId);
		if (pet == null) {
			AuditLogger.info(player, "tried to spawn pet with id " + petId);
			return;
		}
		if (System.currentTimeMillis() - pet.getCommonData().getDespawnTime().getTime() > 10 * 60 * 1000) // reset mood if pet was despawned for > 10 minutes
			player.getPet().getCommonData().clearMoodStatistics();
		player.getPetList().setLastUsedPetId(petId);
	}

	/**
	 * @param player
	 */
	public static final void dismissPet(Player player) {
		Pet toyPet = player.getPet();
		if (toyPet != null) {
			PetFeedProgress progress = toyPet.getCommonData().getFeedProgress();
			if (progress != null) {
				toyPet.getCommonData().setCancelFeed(true);
				DAOManager.getDAO(PlayerPetsDAO.class).saveFeedStatus(player, toyPet.getPetId(), progress.getHungryLevel().getValue(),
					progress.getDataForPacket(), toyPet.getCommonData().getRefeedTime());
			}
			PetDopingBag bag = toyPet.getCommonData().getDopingBag();
			if (bag != null && bag.isDirty())
				DAOManager.getDAO(PlayerPetsDAO.class).saveDopingBag(player, toyPet.getPetId(), bag);

			player.getController().cancelTask(TaskId.PET_UPDATE);
			toyPet.getCommonData().setDespawnTime(new Timestamp(System.currentTimeMillis()));
			toyPet.getCommonData().savePetMoodData();
			toyPet.getController().delete();
			player.setToyPet(null);
		}
	}
}
