package com.aionemu.gameserver.services.toypet;

import com.aionemu.gameserver.configs.main.PeriodicSaveConfig;
import com.aionemu.gameserver.controllers.PetController;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetSpecialFunction;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PET;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author SVDNESS
 * @version 4.8 [JDK 25]
 */

public class PetSpawnService {

	public static void summonPet(Player player, int templateId) {
		PetCommonData lastPetCommonData;
		if (player.getPet() != null) {
			if (player.getPet().getObjectTemplate().getTemplateId() == templateId) {
				return;
			}
			lastPetCommonData = player.getPet().getCommonData();
			player.getPet().getController().delete();
		} else {
			lastPetCommonData = player.getPetList().getLastUsedPet();
		}
		if (lastPetCommonData != null && lastPetCommonData.getTemplateId() != templateId) {
			lastPetCommonData.clearMoodStatistics();
		}
		player.getController().addTask(TaskId.PET_UPDATE, ThreadPoolManager.getInstance().scheduleAtFixedRate(new PetController.PetUpdateTask(player), PeriodicSaveConfig.PLAYER_PETS * 1000L, PeriodicSaveConfig.PLAYER_PETS * 1000L));
		var pet = VisibleObjectSpawner.spawnPet(player, templateId);
		if (pet == null) {
			AuditLogger.log(player, "tried to spawn invalid pet with id " + templateId + "!");
			return;
		}
		var petCommonData = pet.getCommonData();
		if (petCommonData.getRefeedDelay() > 0) {
			petCommonData.scheduleRefeed(petCommonData.getRefeedDelay());
		} else if (petCommonData.getFeedProgress() != null) {
			petCommonData.getFeedProgress().setHungryLevel(PetHungryLevel.HUNGRY);
		}
		if (System.currentTimeMillis() - petCommonData.getDespawnTime().getTime() > 10 * 60 * 1000) {
			petCommonData.clearMoodStatistics();
		}
		player.getPetList().setLastUsedPetTemplateId(templateId);
		if (petCommonData.isLooting()) {
			PacketSendUtility.sendPacket(player, new SM_PET(PetSpecialFunction.AUTOLOOT, true));
		}
		if (petCommonData.isSelling()) {
			PacketSendUtility.sendPacket(player, new SM_PET(PetSpecialFunction.AUTOSELL, true));
		}
	}
}