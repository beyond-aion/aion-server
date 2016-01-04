package com.aionemu.gameserver.services.serialkillers;

import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatAddFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatRateFunction;
import com.aionemu.gameserver.model.templates.serial_killer.RankPenaltyAttr;
import com.aionemu.gameserver.model.templates.serial_killer.RankRestriction;
import com.aionemu.gameserver.skillengine.change.Func;

/**
 * @author Dtem
 */
public class SerialKillerDebuff implements StatOwner {

	private List<IStatFunction> functions = new FastTable<>();
	private RankRestriction rankRestriction;

	public void applyEffect(Player player, String type, Race race, int rank) {
		if (rank == 0)
			return;

		rankRestriction = DataManager.SERIAL_KILLER_DATA.getRankRestriction(type, race, rank);
		if (rankRestriction == null)
			return;
		if (hasDebuff())
			endEffect(player);

		for (RankPenaltyAttr rankPenaltyAttr : rankRestriction.getPenaltyAttr()) {
			if (rankPenaltyAttr.getFunc().equals(Func.PERCENT))
				functions.add(new StatRateFunction(rankPenaltyAttr.getStat(), rankPenaltyAttr.getValue(), true));
			else
				functions.add(new StatAddFunction(rankPenaltyAttr.getStat(), rankPenaltyAttr.getValue(), true));
		}
		player.getGameStats().addEffect(this, functions);
	}

	public boolean hasDebuff() {
		return !functions.isEmpty();
	}

	public void endEffect(Player player) {
		functions.clear();
		player.getGameStats().endEffect(this);
	}

}
