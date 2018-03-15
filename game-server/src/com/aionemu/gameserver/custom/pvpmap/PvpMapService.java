package com.aionemu.gameserver.custom.pvpmap;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Yeats 06.04.2016.
 */
public class PvpMapService {

	private static final PvpMapService instance = new PvpMapService();
	private static final Logger log = LoggerFactory.getLogger(PvpMapService.class);
	private AtomicBoolean isActive = new AtomicBoolean(true);
	private PvpMapHandler handler = null;
	private final List<Integer> randomBossNpcIds = Arrays.asList(231196, 233740, 235759, 235765, 235763, 235767, 235771, 235619, 235620, 235621, 855822,
		855843, 230857, 230858, 277224, 855776, 219933, 219934, 235975, 855263, 231304);

	public static PvpMapService getInstance() {
		return instance;
	}

	public void init() {
		handler = getOrCreateNewHandler();
	}

	public void onLogin(Player player) {
		if (isActive.get() && isRandomBossAlive()) {
			notifyBossSpawn(player);
		}
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
		return randomBossNpcIds.contains(npc.getNpcId());
	}

	public int getRandomBossId() {
		return Rnd.get(randomBossNpcIds);
	}

	private boolean isRandomBossAlive() {
		return handler != null && handler.isRandomBossAlive();
	}

	private void join(Player p) {
		if (p.getLevel() < 60) {
			PacketSendUtility.sendMessage(p, "The PvP-Map is for players level 60 and above.");
		} else if (p.isInInstance() || p.getWorldId() == 400030000) {
			PacketSendUtility.sendMessage(p, "You cannot enter the PvP-Map while in an instance.");
		} else if (p.getController().isInCombat()) {
			PacketSendUtility.sendMessage(p, "You cannot enter the PvP-Map while in combat.");
		} else if (isActive.get()) {
			PvpMapHandler handler = getOrCreateNewHandler();
			if (handler != null) {
				handler.join(p);
			} else {
				PacketSendUtility.sendMessage(p, "You cannot enter the PvP-Map now.");
			}
		} else {
			PacketSendUtility.sendMessage(p, "The PvP-Map is currently deactivated.");
		}
	}

	public void joinMap(Player p) {
		if (handler != null) {
			if (!handler.isOnMap(p)) {
				join(p);
			}
		} else {
			join(p);
		}
	}

	public void leaveMap(Player p) {
		if (handler != null && handler.isOnMap(p)) {
			if (!handler.leave(p)) {
				PacketSendUtility.sendMessage(p, "You cannot leave the PvP-Map in your current state.");
			}
		}
	}

	public boolean isOnPvPMap(Creature creature) {
		return handler != null && handler.isOnMap(creature);
	}

	public synchronized void closeMap(int instanceId) {
		if (handler != null && handler.getInstanceId() == instanceId) {
			log.info("[PvpMapService] Destroyed PvpMapHandler with instanceId: " + handler.getInstanceId());
			handler = null;
		}
	}

	public int getParticipantsSize() {
		if (!isActive.get() || handler == null) {
			return 0;
		} else {
			return handler.getParticipantsSize();
		}
	}

	private synchronized PvpMapHandler getOrCreateNewHandler() {
		if (handler != null) {
			return handler;
		} else {
			PvpMapHandler mapHandler = null;
			try {
				mapHandler = PvpMapHandler.class.newInstance();
			} catch (Exception e) {
				log.warn("[PvpMapService] Could not create a new instance of PvpMapHandler:", e);
			}
			handler = mapHandler;
			return mapHandler;
		}
	}

	public boolean activate(Player admin) {
		if (isActive.compareAndSet(false, true)) {
			log.info("[PvpMapService] Admin " + admin.getName() + " activated the PvP-Map.");
			return true;
		}
		return false;
	}

	public boolean deactivate(Player admin) {
		if (isActive.compareAndSet(true, false)) {
			if (handler != null) {
				handler.removeAllPlayersAndStop();
			}
			handler = null;
			log.info("[PvpMapService] Admin " + admin.getName() + " deactivated the PvP-Map");
			return true;
		}
		return false;
	}
}
