package com.aionemu.gameserver.model.craft;

import com.aionemu.gameserver.model.Race;

/**
 * @author synchro2
 */
public enum MasterQuestsList {
	COOKING_ELYOS(new int[] { 19039, 19038 }, Race.ELYOS, 40001),
	COOKING_ASMODIANS(new int[] { 29039, 29038 }, Race.ASMODIANS, 40001),
	WEAPONSMITHING_ELYOS(new int[] { 19009, 19008 }, Race.ELYOS, 40002),
	WEAPONSMITHING_ASMODIANS(new int[] { 29009, 29008 }, Race.ASMODIANS, 40002),
	ARMORSMITHING_ELYOS(new int[] { 19015, 19014 }, Race.ELYOS, 40003),
	ARMORSMITHING_ASMODIANS(new int[] { 29015, 29014 }, Race.ASMODIANS, 40003),
	TAILORING_ELYOS(new int[] { 19021, 19020 }, Race.ELYOS, 40004),
	TAILORING_ASMODIANS(new int[] { 29021, 29020 }, Race.ASMODIANS, 40004),
	ALCHEMY_ELYOS(new int[] { 19033, 19032 }, Race.ELYOS, 40007),
	ALCHEMY_ASMODIANS(new int[] { 29033, 29032 }, Race.ASMODIANS, 40007),
	HANDICRAFTING_ELYOS(new int[] { 19027, 19026 }, Race.ELYOS, 40008),
	HANDICRAFTING_ASMODIANS(new int[] { 29027, 29026 }, Race.ASMODIANS, 40008),
	MENUSIER_ELYOS(new int[] { 19058, 19057 }, Race.ELYOS, 40010),
	MENUSIER_ASMODIANS(new int[] { 29058, 29057 }, Race.ASMODIANS, 40010);

	private int[] questIds;
	private Race race;
	private int craftSkillId;

	MasterQuestsList(int[] questIds, Race race, int craftSkillId) {
		this.questIds = questIds;
		this.race = race;
		this.craftSkillId = craftSkillId;
	}

	private Race getRace() {
		return race;
	}

	private int getCraftSkillId() {
		return craftSkillId;
	}

	public static int[] getQuestIds(int craftSkillId, Race race) {
		for (MasterQuestsList mql : values()) {
			if (race == mql.getRace() && craftSkillId == mql.getCraftSkillId())
				return mql.questIds;
		}
		throw new IllegalArgumentException("Invalid craftSkillId: " + craftSkillId + " or race: " + race);
	}
}
