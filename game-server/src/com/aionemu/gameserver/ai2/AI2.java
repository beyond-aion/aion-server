package com.aionemu.gameserver.ai2;

import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer
 */
public interface AI2 {

	void onCreatureEvent(AIEventType event, Creature creature);

	void onCustomEvent(int eventId, Object... args);

	void onGeneralEvent(AIEventType event);

	/**
	 * If already handled dialog return true.
	 */
	boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex);

	void think();

	boolean canThink();

	AIState getState();

	AISubState getSubState();

	String getName();

	/**
	 * Ask AI instance for the answer to the specified question.
	 * 
	 * @param question
	 * @return The answer, true or false.
	 */
	boolean ask(AIQuestion question);

	boolean isLogging();

	long getRemainigTime();

	int modifyDamage(Skill skill, Creature creature, int damage);

	int modifyDamage(Creature creature, int damage);

	int modifyOwnerDamage(int damage);

	int modifyHealValue(int value);

	int modifyMaccuracy(int value);

	int modifyMattack(int value);

	int modifyPdef(int value);

	ItemAttackType modifyAttackType(ItemAttackType type);

	int modifyARange(int value);

	void fireOnEndCastEvents(NpcSkillEntry usedSkill);

	void fireOnStartCastEvents(NpcSkillEntry startingSkill);
}
