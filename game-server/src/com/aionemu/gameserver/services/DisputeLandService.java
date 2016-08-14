package com.aionemu.gameserver.services;

import javolution.util.FastTable;

import com.aionemu.commons.services.CronService;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DISPUTE_LAND;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;
import com.aionemu.gameserver.world.zone.ZoneAttributes;

/**
 * @author Source
 */
public class DisputeLandService {

	private boolean active;
	private FastTable<Integer> worlds = new FastTable<Integer>();
	private static final int chance = CustomConfig.DISPUTE_RND_CHANCE;
	private static final String rnd = CustomConfig.DISPUTE_RND_SCHEDULE;
	private static final String fxd = CustomConfig.DISPUTE_FXD_SCHEDULE;

	private DisputeLandService() {
	}

	public static DisputeLandService getInstance() {
		return DisputeLandServiceHolder.INSTANCE;
	}

	public void init() {
		if (!CustomConfig.DISPUTE_ENABLED) {
			return;
		}

		// Dispute worldId's
		worlds.add(600020000);
		worlds.add(600020001);
		worlds.add(600030000);

		CronService.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				setActive(Rnd.get(1, 100) <= chance);

				if (isActive()) {
					// Disable after 30 mins
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							setActive(false);
						}

					}, 1800 * 1000);
				}
			}

		}, rnd);

		CronService.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				// Disable after 5 hours
				setActive(true);

				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						setActive(false);
					}

				}, 5 * 3600 * 1000);
			}

		}, fxd);
	}

	public boolean isActive() {
		if (!CustomConfig.DISPUTE_ENABLED) {
			return false;
		}

		return active;
	}

	public void setActive(boolean value) {
		active = value;
		syncState();
		broadcast();
	}

	private void syncState() {
		for (int world : worlds) {
			if (world == 600020001) {
				continue;
			}

			if (active) {
				World.getInstance().getWorldMap(world).setWorldOption(ZoneAttributes.PVP_ENABLED);
			} else {
				World.getInstance().getWorldMap(world).removeWorldOption(ZoneAttributes.PVP_ENABLED);
			}
		}
	}

	private void broadcast(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DISPUTE_LAND(worlds, active));
	}

	private void broadcast() {
		World.getInstance().forEachPlayer(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				broadcast(player);
			}

		});
	}

	public void onLogin(Player player) {
		if (!CustomConfig.DISPUTE_ENABLED) {
			return;
		}

		broadcast(player);
	}

	private static class DisputeLandServiceHolder {

		private static final DisputeLandService INSTANCE = new DisputeLandService();
	}

}
