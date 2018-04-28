package com.aionemu.gameserver.model.items;

/**
 * @author Estrayl
 */
public class PendingTuneResult {

	private final int optionalSockets;
	private final int enchantBonus;
	private final int statBonusId;

	public PendingTuneResult(int optionalSockets, int enchantBonus, int statBonusId) {
		this.optionalSockets = optionalSockets;
		this.enchantBonus = enchantBonus;
		this.statBonusId = statBonusId;
	}

	public int getOptionalSockets() {
		return optionalSockets;
	}

	public int getEnchantBonus() {
		return enchantBonus;
	}

	public int getStatBonusId() {
		return statBonusId;
	}

}
