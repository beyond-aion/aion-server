package com.aionemu.gameserver.network.loginserver.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.LsClientPacket;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Aionchs-Wylovech
 */
public class CM_LS_CONTROL_RESPONSE extends LsClientPacket {

	public CM_LS_CONTROL_RESPONSE(int opCode) {
		super(opCode);
	}

	private byte type, param;
	private int accountId, adminId;
	private boolean result;

	@Override
	public void readImpl() {
		type = readC();
		param = readC();
		accountId = readD();
		adminId = readD();
		result = readC() == 1;
	}

	@Override
	public void runImpl() {
		Player admin = World.getInstance().getPlayer(adminId);
		if (!result) {
			if (admin != null)
				PacketSendUtility.sendMessage(admin, "The operation failed.");
			return;
		}
		AionConnection playerConnection = LoginServer.getInstance().accountUpdate(accountId, type, param);
		Player player = playerConnection == null ? null : playerConnection.getActivePlayer();
		String targetAccount = player == null ? "Account " + accountId : "Account of " + player.getName();
		switch (type) {
			case 1 -> notifyAboutNewPermissions(admin, player, targetAccount, "access level");
			case 2 -> notifyAboutNewPermissions(admin, player, targetAccount, "membership level");
			default -> sendMessage(admin, targetAccount + " has been successfully updated.");
		}
	}

	private void notifyAboutNewPermissions(Player admin, Player player, String targetAccount, String permissionType) {
		sendMessage(admin, "%s has been granted %s %s.".formatted(targetAccount, permissionType, param));
		if (admin == null)
			sendMessage(player, "You have been granted %s %s.".formatted(permissionType, param));
		else
			sendMessage(player, "You have been granted %s %s by %s.".formatted(permissionType, param, admin.getName(true)));
	}

	private void sendMessage(Player player, String message) {
		if (player != null)
			PacketSendUtility.sendMessage(player, message);
	}
}
