package com.aionemu.gameserver.services.toypet;

import com.aionemu.gameserver.configs.main.PeriodicSaveConfig;
import com.aionemu.gameserver.controllers.PetController;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
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
			player.getPet().getController().delete();
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

}
