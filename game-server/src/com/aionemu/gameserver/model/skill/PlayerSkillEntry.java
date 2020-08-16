package com.aionemu.gameserver.model.skill;

import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_REMOVE;
import com.aionemu.gameserver.skillengine.model.SkillLearnTemplate;
import com.aionemu.gameserver.skillengine.model.StigmaType;

/**
 * @author ATracer, Neon
 */
public class PlayerSkillEntry extends SkillEntry implements Persistable {

	private int skillType; // 0 normal skill , 1 stigma skill , 3 linked stigma skill
	private volatile int currentXp; // for crafting skills
	private PersistentState persistentState;

	public PlayerSkillEntry(Player player, int skillId, int skillLvl, PersistentState persistentState) {
		this(skillId, skillLvl, 0, persistentState);
		List<SkillLearnTemplate> learnTemplates = DataManager.SKILL_TREE_DATA.getTemplatesForSkill(skillId, player.getPlayerClass(), player.getRace());
		if (learnTemplates.size() == 0)
			skillType = DataManager.SKILL_DATA.getSkillTemplate(skillId).getStigmaType() == StigmaType.NONE ? 0 : 1; // no way to tell if linked stigma
		else {
			for (SkillLearnTemplate template : learnTemplates) {
				if (template.isStigma()) {
					skillType = template.isLinkedStigma() ? 3 : 1;
					break;
				}
			}
		}
	}

	public PlayerSkillEntry(int skillId, int skillLvl, int skillType, PersistentState persistentState) {
		super(skillId, skillLvl);
		this.skillType = skillType;
		this.persistentState = persistentState;
	}

	public boolean isStigmaSkill() {
		return skillType > 0;
	}

	public boolean isNormalStigmaSkill() {
		return isStigmaSkill() && !isLinkedStigmaSkill();
	}

	public boolean isLinkedStigmaSkill() {
		return skillType >= 3;
	}

	public boolean isNormalSkill() {
		return !isStigmaSkill() && skillId < 30000;
	}

	public boolean isNormalOrStigmaSkill() {
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
	 * @return The flag that the client wants for the skill.
	 */
	public int getProfessionFlag() {
		if (isTappingSkill() || isMorphSkill())
			return 1; // not sure for morph
		if (isCraftingSkill())
			return getCurrentXp(); // not implemented in DB
		return 0;
	}

	public int getFlag() {
		return isNormalSkill() ? getDateLearned() : 0;
	}

	public int getDateLearned() {
		return (int) (System.currentTimeMillis() / 1000); // not implemented in DB
	}

	public int getSkillType() {
		return skillType;
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
			size += (skillLevel - 350) / 100; // above 400 points, the crafting max points increase by 50 instead of 100
		return isTappingSkill() ? Math.min(size, 4) : size; // limit tapping bar size to 4 (499) to prevent black bar above 500 points
	}

	public int getCurrentXp() {
		return currentXp;
	}

	public void setCurrentXp(int currentXp) {
		this.currentXp = currentXp;
	}

	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	@Override
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
