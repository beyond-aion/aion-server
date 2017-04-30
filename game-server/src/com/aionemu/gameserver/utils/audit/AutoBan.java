package com.aionemu.gameserver.utils.audit;

import com.aionemu.gameserver.configs.main.PunishmentConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.BannedMacManager;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.services.PunishmentService;

/**
 * @author synchro2
 */
public class AutoBan {

	// TODO merge with AntiHackService punishment system / rework
	protected static void punishment(Player player) {
		String reason = "You have been punished due to illegal actions";
		String accountIp = player.getClientConnection().getIP();
		int accountId = player.getClientConnection().getAccount().getId();
		int time = PunishmentConfig.PUNISHMENT_TIME;
		int minInDay = 1440;
		int dayCount = (int) (Math.floor(time / minInDay));

		switch (PunishmentConfig.PUNISHMENT_TYPE) {
			case 1:
				player.getClientConnection().close(new SM_QUIT_RESPONSE());
				break;
			case 2:
				PunishmentService.banChar(player.getObjectId(), dayCount, reason);
				break;
			case 3:
				LoginServer.getInstance().sendBanPacket((byte) 1, accountId, accountIp, time, 0);
				break;
			case 4:
				LoginServer.getInstance().sendBanPacket((byte) 2, accountId, accountIp, time, 0);
				break;
			case 5:
				player.getClientConnection().close();
				BannedMacManager.getInstance().banAddress(player.getClientConnection().getMacAddress(), System.currentTimeMillis() + time * 60000, reason);
				break;
		}
	}
}
