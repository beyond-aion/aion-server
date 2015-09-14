package com.aionemu.gameserver.services.rift;

import java.util.Map;

import com.aionemu.gameserver.model.rift.RiftLocation;
import com.aionemu.gameserver.services.RiftService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Source
 */
public class RiftOpenRunnable implements Runnable {

	private final int worldId;
	private final boolean guards;

	public RiftOpenRunnable(int worldId, boolean guards) {
		this.worldId = worldId;
		this.guards = guards;
	}

	@Override
	public void run() {
		Map<Integer, RiftLocation> locations = RiftService.getInstance().getRiftLocations();
		for (RiftLocation loc : locations.values()) {
			if (loc.getWorldId() == worldId) {
				RiftService.getInstance().openRifts(loc, guards);
			}
		}

		// Scheduled rifts close
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				RiftService.getInstance().closeRifts();
			}

		}, RiftService.getInstance().getDuration() * 3540 * 1000);
		// Broadcast rift spawn on map
		RiftInformer.sendRiftsInfo(worldId);
	}

}
