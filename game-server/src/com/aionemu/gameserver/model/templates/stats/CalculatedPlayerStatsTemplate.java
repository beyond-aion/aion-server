package com.aionemu.gameserver.model.templates.stats;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.utils.stats.ClassStats;

/**
 * @author ATracer
 */
public class CalculatedPlayerStatsTemplate extends PlayerStatsTemplate {

	private PlayerClass playerClass;

	public CalculatedPlayerStatsTemplate(PlayerClass playerClass) {
		this.playerClass = playerClass;
	}

	@Override
	public int getBaseAccuracy() {
		return ClassStats.getBaseAccuracyFor(playerClass);
	}

	@Override
	public int getAgility() {
		return ClassStats.getAgilityFor(playerClass);
	}

	@Override
	public int getHealth() {
		return ClassStats.getHealthFor(playerClass);
	}

	@Override
	public int getKnowledge() {
		return ClassStats.getKnowledgeFor(playerClass);
	}

	@Override
	public int getPower() {
		return ClassStats.getPowerFor(playerClass);
	}

	@Override
	public int getWill() {
		return ClassStats.getWillFor(playerClass);
	}

	@Override
	public int getBlock() {
		return ClassStats.getBlockFor(playerClass);
	}

	@Override
	public int getEvasion() {
		return ClassStats.getEvasionFor(playerClass);
	}

	@Override
	public float getFlySpeed() {
		return ClassStats.getFlySpeedFor(playerClass);
	}

	@Override
	public int getMacc() {
		return ClassStats.getMagicAccuracyFor(playerClass);
	}

	@Override
	public int getAccuracy() {
		return ClassStats.getAccuracyFor(playerClass);
	}

	@Override
	public int getAttack() {
		return ClassStats.getAttackFor(playerClass);
	}

	@Override
	public int getPcrit() {
		return ClassStats.getCritFor(playerClass);
	}

	@Override
	public int getMaxHp() {
		return ClassStats.getMaxHpFor(playerClass, 10); // level is hardcoded
	}

	@Override
	public int getMaxMp() {
		return 1000;
	}

	@Override
	public int getParry() {
		return ClassStats.getParryFor(playerClass);
	}

	@Override
	public float getRunSpeed() {
		return ClassStats.getSpeedFor(playerClass);
	}

	@Override
	public float getWalkSpeed() {
		return 1.5f;
	}

}
