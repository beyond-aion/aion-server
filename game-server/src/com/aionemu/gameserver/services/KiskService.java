package com.aionemu.gameserver.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.animations.ActionAnimation;
import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ACTION_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_KISK_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Sarynth, nrg, Sykra
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
		// remove the "2h place cooldown" for the kisk creator
		Player creator = World.getInstance().getPlayer(kisk.getCreatorId());
		if (creator != null)
			PacketSendUtility.sendPacket(creator, new SM_KISK_UPDATE(kisk));

		for (Player member : kisk.getCurrentMemberList()) {
			TeleportService.sendKiskBindPoint(member);
			member.setKisk(null);
			if (member.isDead()) // player has died and is not revived
				member.getController().showResurrectionOptions();
		}
	}

	public void onBind(Kisk kisk, Player player) {
		if (player.getKisk() != null)
			player.getKisk().removePlayer(player);

		kisk.addPlayer(player);
		TeleportService.sendKiskBindPoint(player);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_BINDSTONE_REGISTER());
		// Send Animated Bind Flash
		PacketSendUtility.broadcastPacket(player, new SM_ACTION_ANIMATION(player.getObjectId(), ActionAnimation.BIND_KISK), true);
	}

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
