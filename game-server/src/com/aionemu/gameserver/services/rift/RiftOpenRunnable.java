package com.aionemu.gameserver.services.rift;

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
		RiftService.getInstance().prepareRiftOpening(worldId, guards);
		// Scheduled rifts close
		ThreadPoolManager.getInstance().schedule(() -> RiftService.getInstance().closeRifts(false),
			RiftService.getInstance().getDuration() * 3540 * 1000);
		// Broadcast rift spawn on map
		RiftInformer.sendRiftsInfo(worldId);
	}
}
