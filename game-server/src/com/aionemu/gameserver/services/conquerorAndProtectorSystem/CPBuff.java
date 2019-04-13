package com.aionemu.gameserver.services.conquerorAndProtectorSystem;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.templates.cp.CPRank;
import com.aionemu.gameserver.model.templates.cp.CPType;

/**
 * @author Dtem
 */
public class CPBuff implements StatOwner {

	public void applyEffect(Player player, CPType type, int rank) {
		endEffect(player);

		if (rank == 0)
			return;

		CPRank cpRank = DataManager.CONQUEROR_AND_PROTECTOR_DATA.getRank(type, rank);
		if (cpRank != null && !cpRank.getStatModifiers().isEmpty())
			player.getGameStats().addEffect(this, cpRank.getStatModifiers());
	}

	public void endEffect(Player player) {
		player.getGameStats().endEffect(this);
	}
}
