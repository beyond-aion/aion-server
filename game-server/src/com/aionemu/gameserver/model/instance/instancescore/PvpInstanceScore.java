package com.aionemu.gameserver.model.instance.instancescore;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;

public class PvpInstanceScore<T extends InstancePlayerReward> extends InstanceScore<T> {

	private final int winnerApReward;
	private final int loserApReward;
	private final int drawApReward;
	private int asmodiansPoints;
	private int elyosPoints;
	private int asmodiansKills;
	private int elyosKills;

	public PvpInstanceScore(int winnerApReward, int loserApReward, int drawApReward) {
		this.winnerApReward = winnerApReward;
		this.loserApReward = loserApReward;
		this.drawApReward = drawApReward;
	}

	public int getWinnerApReward() {
		return winnerApReward;
	}

	public int getLoserApReward() {
		return loserApReward;
	}

	public int getDrawApReward() {
		return drawApReward;
	}

	public int getAsmodiansPoints() {
		return asmodiansPoints;
	}

	public int getElyosPoints() {
		return elyosPoints;
	}

	public int getPointsByRace(Race race) {
		return race == Race.ELYOS ? elyosPoints : asmodiansPoints;
	}

	public int getAsmodiansKills() {
		return asmodiansKills;
	}

	public int getElyosKills() {
		return elyosKills;
	}

	public Race getRaceWithHighestPoints() {
		return elyosPoints == asmodiansPoints ? Race.NONE : elyosPoints > asmodiansPoints ? Race.ELYOS : Race.ASMODIANS;
	}

	public synchronized void addPointsByRace(Race race, int points) {
		switch (race) {
			case ELYOS -> elyosPoints = Math.max(0, elyosPoints + points);
			case ASMODIANS -> asmodiansPoints = Math.max(0, asmodiansPoints + points);
		}
	}

	public synchronized void incrementKillsByRace(Race race) {
		switch (race) {
			case ELYOS -> elyosKills++;
			case ASMODIANS -> asmodiansKills++;
		}
	}
}
