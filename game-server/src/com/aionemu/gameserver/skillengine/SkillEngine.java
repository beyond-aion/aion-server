package com.aionemu.gameserver.skillengine;

import com.aionemu.gameserver.skillengine.model.*;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.skillengine.model.Effect.ForceType;

/**
 * @author ATracer
 */
public class SkillEngine {

	private static final SkillEngine skillEngine = new SkillEngine();

	/**
	 * should not be instantiated directly
	 */
	private SkillEngine() {
	}

	/**
	 * This method is used for skills that were learned by player
	 * 
	 * @param player
	 * @param skillId
	 * @return Skill
	 */
	public Skill getSkillFor(Player player, int skillId, VisibleObject firstTarget) {
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);

		if (template == null)
			return null;

		return getSkillFor(player, template, firstTarget);
	}

	/**
	 * This method is used for skills that were learned by player
	 * 
	 * @param player
	 * @param template
	 * @param firstTarget
	 * @return
	 */
	public Skill getSkillFor(Player player, SkillTemplate template, VisibleObject firstTarget) {
		// player doesn't have such skill and ist not provoked
		if (template.getActivationAttribute() != ActivationAttribute.PROVOKED) {
			if (!player.getSkillList().isSkillPresent(template.getSkillId()))
				return null;
		}

		Creature target = null;
		if (firstTarget instanceof Creature)
			target = (Creature) firstTarget;

		return new Skill(template, player, target);
	}

	public Skill getSkillFor(Player player, SkillTemplate template, VisibleObject firstTarget, int skillLevel) {
		Creature target = null;
		if (firstTarget instanceof Creature)
			target = (Creature) firstTarget;

		return new Skill(template, player, target, skillLevel);
	}

	/**
	 * This method is used for not learned skills (item skills etc)
	 * 
	 * @param creature
	 * @param skillId
	 * @param skillLevel
	 * @return Skill
	 */
	public Skill getSkill(Creature creature, int skillId, int skillLevel, VisibleObject firstTarget) {
		return getSkill(creature, skillId, skillLevel, firstTarget, null);
	}

	public Skill getSkill(Creature creature, int skillId, int skillLevel, VisibleObject firstTarget, ItemTemplate itemTemplate) {
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);

		if (template == null)
			return null;

		Creature target = null;
		if (firstTarget instanceof Creature)
			target = (Creature) firstTarget;
		return new Skill(template, creature, skillLevel, target, itemTemplate);
	}

	public ChargeSkill getChargeSkill(Player creature, int skillId, int skillLevel, VisibleObject firstTarget) {
		return getChargeSkill(creature, skillId, skillLevel, firstTarget, null);
	}

	public ChargeSkill getChargeSkill(Player creature, int skillId, int skillLevel, VisibleObject firstTarget, ItemTemplate itemTemplate) {
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);

		if (template == null)
			return null;

		Creature target = null;
		if (firstTarget instanceof Creature)
			target = (Creature) firstTarget;
		return new ChargeSkill(template, creature, skillLevel, target, itemTemplate);
	}

	public PenaltySkill getPenaltySkill(Creature effector, int skillId, int skillLevel) {
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		if (template == null)
			return null;

		return new PenaltySkill(template, effector, skillLevel);
	}


	public static SkillEngine getInstance() {
		return skillEngine;
	}

	public Effect applyEffectDirectly(int skillId, Creature effector, Creature effected) {
		SkillTemplate skillTemplate = checkAndGetSkillTemplate(skillId);
		return skillTemplate == null ? null : applyEffect(effector, effected, skillTemplate, skillTemplate.getLvl(), null, ForceType.DEFAULT);
	}

	/**
	 * This method is used to apply effects of given skill directly without checking properties. Should be only used from handlers, or when you are sure
	 * about it
	 * 
	 * @param duration
	 *          - null = calculates native duration, number = uses forced duration value (0 meaning permanent)
	 * @param forceType
	 *          - null = not forced, else the force identifier (can later be retrieved by {@link Effect#getForceType()})
	 */
	public Effect applyEffectDirectly(int skillId, Creature effector, Creature effected, Integer duration, ForceType forceType) {
		SkillTemplate skillTemplate = checkAndGetSkillTemplate(skillId);
		return skillTemplate == null ? null : applyEffect(effector, effected, skillTemplate, skillTemplate.getLvl(), duration, forceType);
	}

	public Effect applyEffectDirectly(int skillId, int lvl, Creature effector, Creature effected, Integer duration, ForceType forceType) {
		SkillTemplate skillTemplate = checkAndGetSkillTemplate(skillId);
		return skillTemplate == null ? null : applyEffect(effector, effected, skillTemplate, lvl, duration, forceType);
	}

	public Effect applyEffectDirectly(SkillTemplate skillTemplate, int skillLevel, Creature effector, Creature effected) {
		return applyEffect(effector, effected, skillTemplate, skillLevel, null, ForceType.DEFAULT);
	}

	/**
	 * Similar function to {@link #applyEffectDirectly}, but effect is not forced. That means it checks for resists etc.
	 */
	public Effect applyEffect(int skillId, Creature effector, Creature effected) {
		SkillTemplate skillTemplate = checkAndGetSkillTemplate(skillId);
		return skillTemplate == null ? null : applyEffect(effector, effected, skillTemplate, skillTemplate.getLvl(), null, null);
	}

	private Effect applyEffect(Creature effector, Creature effected, SkillTemplate skillTemplate, int lvl, Integer duration, ForceType forceType) {
		Effect ef = new Effect(effector, effected, skillTemplate, lvl, duration, forceType);
		ef.initialize();
		ef.applyEffect();
		return ef;
	}

	private SkillTemplate checkAndGetSkillTemplate(int skillId) {
		SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		if (skillTemplate == null) {
			LoggerFactory.getLogger(SkillEngine.class).warn("Could not apply effect, invalid skill id " + skillId, new IllegalArgumentException());
			return null;
		}
		return skillTemplate;
	}

}
