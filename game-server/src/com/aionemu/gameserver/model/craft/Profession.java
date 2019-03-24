package com.aionemu.gameserver.model.craft;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.utils.ChatUtil;

/**
 * @author Neon
 */
public enum Profession {

	ESSENCETAPPING(30002),
	AETHERTAPPING(30003),
	COOKING(40001),
	WEAPONSMITHING(40002),
	ARMORSMITHING(40003),
	TAILORING(40004),
	// LEATHERWORK(40005),
	// CARPENTRY(40006),
	ALCHEMY(40007),
	HANDICRAFTING(40008),
	CONSTRUCTION(40010);

	private final int skillId;

	Profession(int skillId) {
		this.skillId = skillId;
	}

	public int getSkillId() {
		return skillId;
	}

	public boolean isCrafting() {
		return skillId >= 40001 && skillId <= 40010;
	}

	public Integer getUpgradeCost(int skillLevel) {
		switch (skillLevel) {
			case 0:
				return 3500;
			case 99:
				return 17000;
			case 199:
				return 115000;
			case 299:
				return 460000;
			case 449:
				return isCrafting() ? 6004900 : null; // essence- and aethertapping have no artisan grade between expert and master
		}
		return null;
	}

	public int getMaxUpgradableLevel() {
		return isCrafting() ? 499 : 399;
	}

	public String getClientName() {
		return DataManager.SKILL_DATA.getSkillTemplate(skillId).getL10n();
	}

	public String getClientName(int skillLevel) {
		return getSkillGrade(skillLevel) + " " + getClientName();
	}

	private String getSkillGrade(int skillLevel) {
		if (skillLevel <= 99)
			return ChatUtil.l10n(900797); // Amateur
		if (skillLevel <= 199)
			return ChatUtil.l10n(900798); // Novice
		if (skillLevel <= 299)
			return ChatUtil.l10n(900799); // Apprentice
		if (skillLevel <= 399)
			return ChatUtil.l10n(900800); // Journeyman
		if (skillLevel <= 449)
			return ChatUtil.l10n(900801); // Expert
		if (isCrafting() && skillLevel <= 499)
			return ChatUtil.l10n(902027); // Artisan
		return ChatUtil.l10n(902028); // Master
	}

	public static Profession getBySkillId(int skillId) {
		for (Profession profession : values()) {
			if (profession.getSkillId() == skillId)
				return profession;
		}
		return null;
	}
}
