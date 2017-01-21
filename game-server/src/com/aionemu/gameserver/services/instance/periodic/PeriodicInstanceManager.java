package com.aionemu.gameserver.services.instance.periodic;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ViAl
 */
public class PeriodicInstanceManager {

	/**
	 * InstanceMaskID - Service
	 */
	private Map<Byte, PeriodicInstance> services;

	private PeriodicInstanceManager() {
		this.services = new HashMap<>();
		if (AutoGroupConfig.AUTO_GROUP_ENABLE) {
			DredgionService.getInstance().initIfEnabled();
			registerInstance(DredgionService.getInstance());

			KamarBattlefieldService.getInstance().initIfEnabled();
			registerInstance(KamarBattlefieldService.getInstance());

			EngulfedOphidianBridgeService.getInstance().initIfEnabled();
			registerInstance(EngulfedOphidianBridgeService.getInstance());

			IronWallFrontService.getInstance().initIfEnabled();
			registerInstance(IronWallFrontService.getInstance());

			IdgelDomeService.getInstance().initIfEnabled();
			registerInstance(IdgelDomeService.getInstance());
		}
	}

	public void registerInstance(PeriodicInstance instance) {
		for (byte maskId : instance.getMaskIds()) {
			this.services.put(maskId, instance);
		}
	}

	public void handleRequest(Player player, byte maskId) {
		if (this.services.containsKey(maskId)) {
			PeriodicInstance instance = this.services.get(maskId);
			instance.showWindow(player);
		} else {
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId));
		}
	}

	public static PeriodicInstanceManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder {

		private static final PeriodicInstanceManager INSTANCE = new PeriodicInstanceManager();
	}

}
