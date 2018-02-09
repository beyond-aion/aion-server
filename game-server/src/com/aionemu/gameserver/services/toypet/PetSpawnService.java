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
	public static final void summonPet(Player player, int templateId) {
		PetCommonData lastPetCommonData;

		if (player.getPet() != null) {
			if (player.getPet().getObjectTemplate().getTemplateId() == templateId)
				return;
			lastPetCommonData = player.getPet().getCommonData();
			dismissPet(player);
		} else {
			lastPetCommonData = player.getPetList().getLastUsedPet();
		}

		if (lastPetCommonData != null && lastPetCommonData.getTemplateId() != templateId) // reset mood if other pet is spawned
			lastPetCommonData.clearMoodStatistics();

		player.getController().addTask(TaskId.PET_UPDATE, ThreadPoolManager.getInstance().scheduleAtFixedRate(new PetController.PetUpdateTask(player),
			PeriodicSaveConfig.PLAYER_PETS * 1000, PeriodicSaveConfig.PLAYER_PETS * 1000));

		Pet pet = VisibleObjectSpawner.spawnPet(player, templateId);
		if (pet == null) {
			AuditLogger.log(player, "tried to spawn invalid pet with id " + templateId);
			return;
		}
		PetCommonData petCommonData = pet.getCommonData();
		if (petCommonData.getRefeedDelay() > 0) {
			petCommonData.setIsFeedingTime(false);
			petCommonData.scheduleRefeed(petCommonData.getRefeedDelay());
		} else if (petCommonData.getFeedProgress() != null)
			petCommonData.getFeedProgress().setHungryLevel(PetHungryLevel.HUNGRY);
		if (System.currentTimeMillis() - pet.getCommonData().getDespawnTime().getTime() > 10 * 60 * 1000) // reset mood if pet was despawned for > 10 minutes
			player.getPet().getCommonData().clearMoodStatistics();
		player.getPetList().setLastUsedPetTemplateId(templateId);
	}

	public static final void dismissPet(Player player) {
		Pet pet = player.getPet();
		if (pet != null) {
			PetCommonData petCommonData = pet.getCommonData();
			petCommonData.cancelRefeedTask();
			PetFeedProgress progress = petCommonData.getFeedProgress();
			if (progress != null) {
				petCommonData.setCancelFeed(true);
				DAOManager.getDAO(PlayerPetsDAO.class).saveFeedStatus(pet.getObjectId(), progress.getHungryLevel().getValue(),
					progress.getDataForPacket(), petCommonData.getRefeedTime());
			}
			PetDopingBag bag = petCommonData.getDopingBag();
			if (bag != null && bag.isDirty())
				DAOManager.getDAO(PlayerPetsDAO.class).saveDopingBag(pet.getObjectId(), bag);

			player.getController().cancelTask(TaskId.PET_UPDATE);
			petCommonData.setDespawnTime(new Timestamp(System.currentTimeMillis()));
			DAOManager.getDAO(PlayerPetsDAO.class).savePetMoodData(petCommonData);
			pet.getController().delete();
			player.setPet(null);
		}
	}
}
