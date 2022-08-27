package com.aionemu.gameserver.model.instance.instancereward;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;

abstract class AbstractInstancePointsAndKillsReward<T extends InstancePlayerReward> extends AbstractInstancePointsReward<T> {

	private int asmodiansKills, elyosKills;

	AbstractInstancePointsAndKillsReward(int winnerApReward, int loserApReward) {
		super(winnerApReward, loserApReward);
	}

	public synchronized void incrementKillsByRace(Race race) {
		switch (race) {
			case ELYOS -> elyosKills++;
			case ASMODIANS -> asmodiansKills++;
		}
	}

	public int getAsmodiansKills() {
		return asmodiansKills;
	}

	public int getElyosKills() {
		return elyosKills;
	}
}
