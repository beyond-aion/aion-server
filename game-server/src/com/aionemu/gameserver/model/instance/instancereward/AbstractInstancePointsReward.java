package com.aionemu.gameserver.model.instance.instancereward;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;

public abstract class AbstractInstancePointsReward<T extends InstancePlayerReward> extends InstanceReward<T> {

	private final int winnerApReward, loserApReward;
	private int asmodiansPoints, elyosPoints;

	AbstractInstancePointsReward(int winnerApReward, int loserApReward) {
		this.winnerApReward = winnerApReward;
		this.loserApReward = loserApReward;
	}

	public int getWinnerApReward() {
		return winnerApReward;
	}

	public int getLoserApReward() {
		return loserApReward;
	}

	public Race getRaceWithHighestPoints() {
		return elyosPoints >= asmodiansPoints ? Race.ELYOS : Race.ASMODIANS;
	}

	public synchronized void addPointsByRace(Race race, int points) {
		switch (race) {
			case ELYOS -> elyosPoints = Math.max(0, elyosPoints + points);
			case ASMODIANS -> asmodiansPoints = Math.max(0, asmodiansPoints + points);
		}
	}

	public int getAsmodiansPoints() {
		return asmodiansPoints;
	}

	public int getElyosPoints() {
		return elyosPoints;
	}
}
