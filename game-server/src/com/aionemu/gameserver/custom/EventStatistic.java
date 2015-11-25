package com.aionemu.gameserver.custom;

import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Woge
 */
public class EventStatistic {

	private int capturedBases = 0;
	private int playerKills = 0;
	private int deaths = 0;
	private int heal = 0;
	private int damage = 0;
	private int damageTaken = 0;
	private WorldPosition origin;
	private long lastKillTime = System.currentTimeMillis();
	private int streak = 0;

	public EventStatistic(WorldPosition origin) {
		this.origin = origin;
	}

	public int getKillStreak() {
		if ((System.currentTimeMillis() - lastKillTime) <= 45 * 1000) {
			streak++;
		} else {
			streak = 0;
		}
		lastKillTime = System.currentTimeMillis();
		return streak;
	}

	public String getStreakName() {
		int count = getKillStreak();
		switch (count) {
			case 0:
				return "Kill";
			case 1:
				return "*Double Kill*";
			case 2:
				return "*TRIPLE KILL**";
			case 3:
				return "** QUADRA  KILL **";
			case 4:
				return "** P E N T A  K I L L ***";
			case 5:
				return "*** H E X A  K I L L ***";
		}

		if (count > 5) {
			return "HOLY SHIT! killimanjaro!";
		}

		return "Kill";
	}

	public void addDeath() {
		deaths++;
	}

	public WorldPosition getOrigin() {
		return origin;
	}

	public void addKill() {
		playerKills++;
	}

	public void addHeal(int amount) {
		heal = heal + amount;
	}

	public void addDamage(int amount) {
		damage = damage + amount;
	}

	public void addCapture() {
		capturedBases++;
	}

	public void addTakenDamage(int amount) {
		damageTaken = damageTaken + amount;
	}

	/**
	 * @return the capturedBases
	 */
	public int getCapturedBases() {
		return capturedBases;
	}

	/**
	 * @return the playerKills
	 */
	public int getPlayerKills() {
		return playerKills;
	}

	/**
	 * @return the deaths
	 */
	public int getDeaths() {
		return deaths;
	}

	/**
	 * @return the heal
	 */
	public int getHeal() {
		return heal;
	}

	/**
	 * @return the damage
	 */
	public int getDamage() {
		return damage;
	}

	/**
	 * @return the damageTaken
	 */
	public int getDamageTaken() {
		return damageTaken;
	}

}
