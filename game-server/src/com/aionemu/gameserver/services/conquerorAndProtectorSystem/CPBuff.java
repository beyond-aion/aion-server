package com.aionemu.gameserver.services.conquerorAndProtectorSystem;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatAddFunction;
import com.aionemu.gameserver.model.templates.serial_killer.RankPenaltyAttr;
import com.aionemu.gameserver.model.templates.serial_killer.RankRestriction;

/**
 * @author Dtem
 */
public class CPBuff implements StatOwner {

	private List<IStatFunction> functions = new ArrayList<>();
	private RankRestriction rankRestriction;

	public void applyEffect(Player player, String type, Race race, int rank) {
		if (rank == 0)
			return;
		rankRestriction = DataManager.CONQUEROR_AND_PROTECTOR_DATA.getRankRestriction(type, race, rank);
		if (rankRestriction == null) {
			return;
		}
		if (hasDebuff()) {
			endEffect(player);		}

		for (RankPenaltyAttr rankPenaltyAttr : rankRestriction.getPenaltyAttr()) {
				functions.add(new StatAddFunction(rankPenaltyAttr.getStat(), rankPenaltyAttr.getValue(), true));
		}
		player.getGameStats().addEffect(CPBuff.this, functions);
	}

	public boolean hasDebuff() {
		return !functions.isEmpty();
	}

	public void endEffect(Player player) {
		functions.clear();
		player.getGameStats().endEffect(this);
	}

}
