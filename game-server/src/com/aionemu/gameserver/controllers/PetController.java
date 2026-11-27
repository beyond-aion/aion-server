package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.dao.PlayerPetsDAO;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.toypet.PetFeedProgress;
import com.aionemu.gameserver.services.toypet.PetMoodService;

/**
 * @author ATracer
 */
public class PetController extends VisibleObjectController<Pet> {

	@Override
	public void onDelete() {
		super.onDelete();
		PetCommonData commonData = getOwner().getCommonData();
		PetFeedProgress progress = commonData.getFeedProgress();
		commonData.cancelRefeedTask();
		if (progress != null) {
			commonData.setCancelFeed(true);
			PlayerPetsDAO.saveFeedStatus(getOwner().getObjectId(), progress.getHungryLevel().getValue(), progress.getDataForPacket(), commonData.getRefeedTime());
		}
		if (commonData.getDopingBag() != null && commonData.getDopingBag().isDirty())
			PlayerPetsDAO.saveDopingBag(getOwner().getObjectId(), commonData.getDopingBag());

		getOwner().getMaster().getController().cancelTask(TaskId.PET_UPDATE);
		PetMoodService.onDismiss(getOwner().getMaster(), commonData);
		getOwner().getMaster().setPet(null);
	}

	/**
	 * Runs once per minute while the pet is summoned and raises the pet's mood.
	 */
	public static class PetUpdateTask implements Runnable {

		private final Player player;

		public PetUpdateTask(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			Pet pet = player.getPet();
			if (pet == null) {
				player.getController().cancelTask(TaskId.PET_UPDATE);
				return;
			}
			PetMoodService.onMinutePassed(pet);
		}
	}

}
