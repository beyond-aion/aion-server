package com.aionemu.gameserver.model.skill;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_REMOVE;
import com.aionemu.gameserver.skillengine.model.SkillLearnTemplate;
import com.aionemu.gameserver.skillengine.model.StigmaType;

/**
 * @author ATracer
 * @modified Neon
 */
public class PlayerSkillEntry extends SkillEntry {

	private boolean isStigma;
	private int currentXp; // for crafting skills
	private PersistentState persistentState;

	public PlayerSkillEntry(Player player, int skillId, int skillLvl, PersistentState persistentState) {
		this(skillId, skillLvl, false, persistentState);
		SkillLearnTemplate[] learnTemplates = DataManager.SKILL_TREE_DATA.getTemplatesForSkill(skillId);
		if (learnTemplates.length == 0)
			isStigma = DataManager.SKILL_DATA.getSkillTemplate(skillId).getStigmaType() != StigmaType.NONE;
		else {
			for (SkillLearnTemplate template : learnTemplates) {
				if (template.isStigma() && template.getRace() != player.getOppositeRace()
					&& (template.getClassId() == player.getPlayerClass() || template.getClassId() == PlayerClass.ALL)) {
					isStigma = true;
					break;
				}
			}
		}
	}

	public PlayerSkillEntry(int skillId, int skillLvl, boolean isStigma, PersistentState persistentState) {
		super(skillId, skillLvl);
		this.isStigma = isStigma;
		this.persistentState = persistentState;
	}

	public boolean isStigma() {
		return isStigma;
	}

	public boolean isNormalSkill() {
		return !isStigma() && skillId < 30000;
	}

	public boolean isNormalSkillOrStigma() {
		return skillId < 30000;
	}

	public boolean isTappingSkill() {
		return skillId >= 30001 && skillId <= 30003;
	}

	public boolean isCraftingSkill() {
		return skillId >= 40001 && skillId <= 40010 && !isMorphSkill();
	}

	public boolean isMorphSkill() {
		return skillId == 40009;
	}

	public boolean isProfessionSkill() {
		return skillId >= 30000 && skillId < 50000; // 50000 or greater are actions etc.
	}

	/**
	 * Stupid NC shit: For profession skills, these values are also needed in {@link SM_SKILL_REMOVE} to be able to remove the skill from
	 * list.
	 * 
	 * @param skill
	 * @return The flag that the client wants for the skill.
	 */
	public int getProfessionFlag() {
		if (isTappingSkill() || isMorphSkill())
			return 1; // not sure for morph
		if (isCraftingSkill())
			return getCurrentXp(); // not implemented in DB
		return 0;
	}

	public int getFlag(boolean isNew) {
		if (isNormalSkillOrStigma()) {
			if (isNew)
				return isStigma() ? 0 : 1; // highlights the skill in the list
			else
				return getDateLearned();
		}
		return 0;
	}

	public int getDateLearned() {
		return (int) (System.currentTimeMillis() / 1000); // not implemented in DB
	}

	@Override
	public void setSkillLvl(int skillLevel) {
		super.setSkillLvl(skillLevel);
		if (getPersistentState() != PersistentState.NOACTION)
			setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * This number controls the max point number for the professions skill bar. Current values are:<br>
	 * Tapping: 0 = 99, 1 = 199, 2 = 299, 3 = 399, 4 = 499<br>
	 * Crafting: 0 = 99, 1 = 199, 2 = 299, 3 = 399, 4 = 449, 5 = 499, 6 = 549
	 */
	public int getProfessionSkillBarSize() {
		if (!isProfessionSkill())
			return 0;
		int size = skillLevel / 100;
		if (isCraftingSkill() && skillLevel >= 450)
			size += (skillLevel - 400) / 50; // above 400 points, the crafting max points increase by 50 instead of 100
		return isTappingSkill() ? Math.min(size, 4) : size; // limit tapping bar size to 4 (499) to prevent black bar above 500 points
	}

	/**
	 * @return the currentXp
	 */
	public int getCurrentXp() {
		return currentXp;
	}

	/**
	 * @param currentXp
	 *          the currentXp to set
	 */
	public void setCurrentXp(int currentXp) {
		this.currentXp = currentXp;
	}

	/**
	 * @return the pState
	 */
	public PersistentState getPersistentState() {
		return persistentState;
	}

	/**
	 * @param persistentState
	 *          the pState to set
	 */
	public void setPersistentState(PersistentState persistentState) {
		switch (persistentState) {
			case DELETED:
				if (this.persistentState == PersistentState.NEW)
					this.persistentState = PersistentState.NOACTION;
				else
					this.persistentState = PersistentState.DELETED;
				break;
			case UPDATE_REQUIRED:
				if (this.persistentState != PersistentState.NEW)
					this.persistentState = PersistentState.UPDATE_REQUIRED;
				break;
			case NOACTION:
				break;
			default:
				this.persistentState = persistentState;
		}
	}

}
