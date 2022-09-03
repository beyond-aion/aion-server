package com.aionemu.gameserver.services.instance.periodic;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ViAl, Sykra
 */
public class PeriodicInstanceManager {

	private static final PeriodicInstanceManager INSTANCE = new PeriodicInstanceManager();
	private final Map<Integer, PeriodicInstance> instancesByMaskId = new HashMap<>();

	public static PeriodicInstanceManager getInstance() {
		return INSTANCE;
	}

	private PeriodicInstanceManager() {
		if (AutoGroupConfig.AUTO_GROUP_ENABLE) {
			registerServiceAndScheduleRegistration(DredgionService.getInstance());
			registerServiceAndScheduleRegistration(KamarBattlefieldService.getInstance());
			registerServiceAndScheduleRegistration(EngulfedOphidanBridgeService.getInstance());
			registerServiceAndScheduleRegistration(IronWallWarfrontService.getInstance());
			registerServiceAndScheduleRegistration(IdgelDomeService.getInstance());
		}
	}

	private void registerServiceAndScheduleRegistration(PeriodicInstance instance) {
		for (int maskId : instance.getMaskIds())
			instancesByMaskId.put(maskId, instance);
		instance.scheduleRegistrationIfEnabled();
	}

	public void handleRequest(Player player, int maskId) {
		if (instancesByMaskId.containsKey(maskId)) {
			PeriodicInstance instance = instancesByMaskId.get(maskId);
			instance.showWindow(player);
		} else {
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId));
		}
	}
}
