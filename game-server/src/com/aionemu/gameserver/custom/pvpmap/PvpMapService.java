package com.aionemu.gameserver.custom.pvpmap;

import java.awt.Color;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Yeats
 */
public class PvpMapService {

	private static final PvpMapService instance = new PvpMapService();
	private PvpMapHandler handler;

	public static PvpMapService getInstance() {
		return instance;
	}

	public void init() {
		WorldMapInstance instance = InstanceService.getNextAvailableInstance(301220000, 0, (byte) 0, PvpMapHandler::new, 0, false);
		handler = (PvpMapHandler) instance.getInstanceHandler();
	}

	public void onLogin(Player player) {
		if (handler != null && CustomConfig.PVP_MAP_ENABLED && handler.isRandomBossAlive())
			notifyBossSpawn(player);
	}

	public void notifyBossSpawn(Player player) {
		boolean isOnPvpMap = isOnPvPMap(player);
		if (!isOnPvpMap && player.isInInstance()) // don't notify players inside instances
			return;
		boolean isLvSixtyOrHigher = player.getLevel() >= 60;
		if (isOnPvpMap || isLvSixtyOrHigher)
			PacketSendUtility.sendMessage(player, "[PvP-Map] A powerful monster appeared.", ChatType.BRIGHT_YELLOW_CENTER);
		if (!isOnPvpMap && isLvSixtyOrHigher)
			PacketSendUtility.sendMessage(player, "You can join the map via " + ChatUtil.color(".pvp join", Color.WHITE), ChatType.BRIGHT_YELLOW);
	}

	public boolean isRandomBoss(Npc npc) {
		return handler != null && handler.isRandomBoss(npc.getObjectId());
	}

	public void joinMap(Player p) {
		if (handler != null && !handler.isOnMap(p))
			handler.join(p);
	}

	public void leaveMap(Player p) {
		if (handler != null && handler.isOnMap(p))
			handler.leave(p);
	}

	public boolean isOnPvPMap(Creature creature) {
		return handler != null && handler.isOnMap(creature);
	}

	public int getParticipantsSize() {
		return handler == null ? 0 : handler.getParticipantsSize();
	}

	void onInstanceDestroy() {
		handler = null;
	}
}
