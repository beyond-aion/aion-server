package com.aionemu.gameserver.services.abyss;

import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author ViAl, Sykra
 */
public class GloryPointsService {

	private GloryPointsService() {
	}

	public static void addGp(int playerObjId, int amount) {
		if (amount == 0)
			return;
		Player player = World.getInstance().getPlayer(playerObjId);
		boolean addToStats = amount > 0;
		if (player == null) {
			AbyssRankDAO.addGp(playerObjId, amount, addToStats);
		} else {
			int oldGp = player.getAbyssRank().getCurrentGP();
			player.getAbyssRank().addGp(amount, addToStats);
			int added = player.getAbyssRank().getCurrentGP() - oldGp;

			SM_SYSTEM_MESSAGE msg = amount >= 0 ? SM_SYSTEM_MESSAGE.STR_MSG_GLORY_POINT_GAIN(added) : SM_SYSTEM_MESSAGE.STR_MSG_GLORY_POINT_LOSE(-added);
			PacketSendUtility.sendPacket(player, msg);
			if (added != 0)
				PacketSendUtility.sendPacket(player, new SM_ABYSS_RANK(player));
		}
	}
}
