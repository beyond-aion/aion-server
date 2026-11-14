package com.aionemu.gameserver.controllers;

import java.sql.Timestamp;

import com.aionemu.gameserver.dao.PlayerPetsDAO;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetMoodAction;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FUNCTIONAL_PET;
import com.aionemu.gameserver.services.toypet.PetFeedProgress;
import com.aionemu.gameserver.utils.PacketSendUtility;

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
		commonData.setDespawnTime(new Timestamp(System.currentTimeMillis()));
		PlayerPetsDAO.savePetMoodData(commonData);
		getOwner().getMaster().setPet(null);
	}

	public static class PetUpdateTask implements Runnable {

		private final Player player;
		private long startTime = 0;

		public PetUpdateTask(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			if (startTime == 0)
				startTime = System.currentTimeMillis();

			try {
				if (!player.isSpawned())
					return;

				Pet pet = player.getPet();
				if (pet == null)
					throw new IllegalStateException("Pet is null");

				if (System.currentTimeMillis() - startTime >= 60 * 1000) {
					PlayerPetsDAO.savePetMoodData(pet.getCommonData());
					startTime = System.currentTimeMillis();
				}
				PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.mood(pet, PetMoodAction.UPDATE, 0));
			} catch (Exception ex) {
				player.getController().cancelTask(TaskId.PET_UPDATE);
			}
		}
	}

}
