package com.aionemu.gameserver.model.stats.container;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author ATracer
 */
public class TrapGameStats extends NpcGameStats {

	public TrapGameStats(Npc owner) {
		super(owner);
	}

	@Override
	public Stat2 getStat(StatEnum statEnum, float base, CalculationType... calculationTypes) {
		Stat2 stat = super.getStat(statEnum, base, calculationTypes);
		if (owner.getMaster() == null)
			return stat;
		switch (statEnum) {
			case BOOST_MAGICAL_SKILL:
			case MAGICAL_ACCURACY:
				// bonus is calculated from stat bonus of master (only green value)
				stat.setBonusRate(0.7f); // TODO: retail formula?
				return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);

		}
		return stat;
	}

	@Override
	public Stat2 getAttackRange() {
		int base = 5;
		String ownerName = owner.getName();
		if (ownerName.equals("destruction trap") || ownerName.equals("explosion trap") || ownerName.equals("sandstorm trap")
			|| ownerName.equals("skybound trap") || ownerName.equals("spike bite trap") || ownerName.equals("storm mine")
			|| ownerName.equals("scrapped mechanisms")) {
			base = 10;
		} else if (ownerName.equals("trap of clairvoyance")) {
			base = 30;
		} else if (ownerName.equals("propelling trap")) {
			base = 3;
		}
		return getStat(StatEnum.ATTACK_RANGE, base);
	}

	@Override
	public Stat2 getMAccuracy() {
		int value = 1000;
		switch (owner.getName()) {
			case "destruction trap":
				value = 1876;
				break;
			case "spike bite trap":
			case "explosion trap":
			case "spike trap":
			case "sleep trap":
			case "sandstorm trap":
			case "propelling trap":
			case "poisoning trap":
			case "trap of slowing":
			case "blazing trap":
			case "glue trap": //spike trap
			case "trap of dust": //sandstorm
			case "shock trap": //propelling trap
			case "trap of sleep":
			case "trap of burst":
			case "collision trap":
				value = 2361;
				break;
			case "storm mine":
			case "skybound trap":
			case "trap of vengeful spirit": //skybound trap
				value = 2406;
				break;
			case "trap of clairvoyance":
				value = 1050;
				break;
			case "snare trap":
			case "scrapped mechanisms":
				value = 2528;
				break;
		}
		return getStat(StatEnum.MAGICAL_ACCURACY, value);
	}
}
