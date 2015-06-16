package com.aionemu.gameserver.model.skill;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author ATracer
 */
public interface SkillList<T extends Creature> {

	/**
	 * Add skill to list
	 * 
	 * @return true if operation was successful
	 */
	boolean addSkill(T creature, int skillId, int skillLevel);

	/**
	 * Remove skill from list
	 * 
	 * @return true if operation was successful
	 */
	boolean removeSkill(int skillId);

	/**
	 * Check whether skill is present in list
	 */
	boolean isSkillPresent(int skillId);

	int getSkillLevel(int skillId);

	/**
	 * Size of skill list
	 */
	int size();

}
