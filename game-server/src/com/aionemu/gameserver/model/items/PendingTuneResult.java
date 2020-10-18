package com.aionemu.gameserver.model.items;

/**
 * @author Estrayl
 */
public class PendingTuneResult {

	private final int optionalSockets;
	private final int enchantBonus;
	private final int statBonusId;
	private final boolean attributeOnly;

	public PendingTuneResult(int optionalSockets, int enchantBonus, int statBonusId, boolean attributeOnly) {
		this.optionalSockets = optionalSockets;
		this.enchantBonus = enchantBonus;
		this.statBonusId = statBonusId;
		this.attributeOnly = attributeOnly;
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

	public boolean isAttributeOnly() {
		return attributeOnly;
	}
}
