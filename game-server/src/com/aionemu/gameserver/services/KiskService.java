package com.aionemu.gameserver.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_BIND_POINT_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEVEL_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Sarynth, nrg
 */
public class KiskService {

	private static final KiskService instance = new KiskService();
	private final Map<Integer, Kisk> boundButOfflinePlayer = new ConcurrentHashMap<>();
	private final Map<Integer, Kisk> ownerPlayer = new ConcurrentHashMap<>();

	/**
	 * Remove kisk references and containers.
	 * 
	 * @param kisk
	 */
	public void removeKisk(Kisk kisk) {
		// remove offline binds
		for (int memberId : kisk.getCurrentMemberIds()) {
			boundButOfflinePlayer.remove(memberId);
		}

		for (Integer obj : ownerPlayer.keySet()) {
			if (ownerPlayer.get(obj).equals(kisk)) {
				ownerPlayer.remove(obj);
				break;
			}
		}

		// send players SET_BIND_POINT and send them die packet again, if they lie dead, but are still not revived
		for (Player member : kisk.getCurrentMemberList()) {
			member.setKisk(null);
			PacketSendUtility.sendPacket(member, new SM_BIND_POINT_INFO(0, 0f, 0f, 0f, member));
			if (member.getLifeStats().isAlreadyDead())
				member.getController().sendDie();
		}
	}

	/**
	 * @param kisk
	 * @param player
	 */
	public void onBind(Kisk kisk, Player player) {
		if (player.getKisk() != null)
			player.getKisk().removePlayer(player);

		kisk.addPlayer(player);

		// Send Bind Point Data
		TeleportService.sendSetBindPoint(player);

		// Send System Message
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_BINDSTONE_REGISTER());

		// Send Animated Bind Flash
		PacketSendUtility.broadcastPacket(player, new SM_LEVEL_UPDATE(player.getObjectId(), 2, player.getCommonData().getLevel()), true);
	}

	/**
	 * @param player
	 */
	public void onLogin(Player player) {
		Kisk kisk = this.boundButOfflinePlayer.get(player.getObjectId());
		if (kisk != null) {
			kisk.addPlayer(player);
			this.boundButOfflinePlayer.remove(player.getObjectId());
		}
	}

	public void onLogout(Player player) {
		Kisk kisk = player.getKisk();
		// store binding if existent
		if (kisk != null) {
			this.boundButOfflinePlayer.put(player.getObjectId(), kisk);
		}
	}

	public void regKisk(Kisk kisk, Integer objOwnerId) {
		ownerPlayer.put(objOwnerId, kisk);
	}

	public boolean haveKisk(Integer objOwnerId) {
		return ownerPlayer.containsKey(objOwnerId);
	}

	public static KiskService getInstance() {
		return instance;
	}
}
