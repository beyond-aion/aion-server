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
import com.aionemu.gameserver.network.aion.serverpackets.SM_PET;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class PetSpawnService {

	/**
	 * @param player
	 * @param petId
	 */
	public static final void summonPet(Player player, int petId, boolean isManualSpawn) {
		PetCommonData lastPetCommonData;

		if (player.getPet() != null) {
			if (player.getPet().getPetId() == petId) {
				PacketSendUtility.broadcastPacket(player, new SM_PET(3, player.getPet()), true);
				return;
			}

			lastPetCommonData = player.getPet().getCommonData();
			dismissPet(player, isManualSpawn);
		} else {
			lastPetCommonData = player.getPetList().getLastUsedPet();
		}

		if (lastPetCommonData != null) {
			// reset mood if other pet is spawned
			if (petId != lastPetCommonData.getPetId())
				lastPetCommonData.clearMoodStatistics();
		}

		player.getController().addTask(
			TaskId.PET_UPDATE,
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new PetController.PetUpdateTask(player), PeriodicSaveConfig.PLAYER_PETS * 1000,
				PeriodicSaveConfig.PLAYER_PETS * 1000));

		Pet pet = VisibleObjectSpawner.spawnPet(player, petId);
		// It means serious error or cheater - why its just nothing say "null"?
		if (pet != null) {
			if (System.currentTimeMillis() - pet.getCommonData().getDespawnTime().getTime() > 10 * 60 * 1000) {
				// reset mood if pet was despawned for longer than 10 mins.
				player.getPet().getCommonData().clearMoodStatistics();
			}
			lastPetCommonData = pet.getCommonData();
			player.getPetList().setLastUsedPetId(petId);
		}
	}

	/**
	 * @param player
	 * @param isManualDespawn
	 */
	public static final void dismissPet(Player player, boolean isManualDespawn) {
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

			// TODO needs for pet teleportation
			if (isManualDespawn)
				toyPet.getCommonData().setDespawnTime(new Timestamp(System.currentTimeMillis()));

			toyPet.getCommonData().savePetMoodData();

			player.setToyPet(null);
			toyPet.getController().onDelete();
		}

	}
}
