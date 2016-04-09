package com.aionemu.gameserver.custom.pvpmap;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Yeats 06.04.2016.
 */
public class PvpMapService {

	private static final PvpMapService instance = new PvpMapService();
	private static final Logger log = LoggerFactory.getLogger(PvpMapService.class);
	private AtomicBoolean isActive = new AtomicBoolean(true);
	private PvpMapHandler handler = null;

	public static PvpMapService getInstance() {
		return instance;
	}

	private void join(Player p) {
		if (p.getLevel() < 60) {
			PacketSendUtility.sendMessage(p, "The PvP-Map is for players level 60 and above.");
		} else if (p.isInInstance() || p.getPanesterraTeam() != null) {
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

	public void joinOrLeave(Player p) {
		if (handler != null) {
			if (handler.isOnMap(p)) {
				if (!handler.leave(p)) {
					PacketSendUtility.sendMessage(p, "You cannot leave the PvP-Map in your current state.");
				}
			} else {
				join(p);
			}
		} else {
			join(p);
		}
	}

	public boolean isOnPvPMap(Player p) {
		if (handler != null) {
			return handler.isOnMap(p);
		}
		return false;
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

	public synchronized PvpMapHandler getOrCreateNewHandler() {
		if (handler != null) {
			return handler;
		} else {
			PvpMapHandler mapHandler = null;
			try {
				mapHandler = PvpMapHandler.class.newInstance();
			} catch (Exception e) {
				log.warn("[PvpMapService] Could not create a new instance of PvpMapHandler:");
				e.printStackTrace();
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
