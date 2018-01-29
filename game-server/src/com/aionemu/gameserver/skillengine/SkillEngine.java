package com.aionemu.gameserver.skillengine;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.skillengine.model.ActivationAttribute;
import com.aionemu.gameserver.skillengine.model.ChargeSkill;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

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
		return getSkill(creature, skillId, skillLevel, firstTarget, null, false);
	}

	public Skill getSkill(Creature creature, int skillId, int skillLevel, VisibleObject firstTarget, boolean isPenalty) {
		return getSkill(creature, skillId, skillLevel, firstTarget, null, isPenalty);
	}

	public Skill getSkill(Creature creature, int skillId, int skillLevel, VisibleObject firstTarget, ItemTemplate itemTemplate, boolean isPenalty) {
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);

		if (template == null)
			return null;

		Creature target = null;
		if (firstTarget instanceof Creature)
			target = (Creature) firstTarget;
		return new Skill(template, creature, skillLevel, target, itemTemplate, isPenalty);
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

	public static SkillEngine getInstance() {
		return skillEngine;
	}

	public void applyEffectDirectly(int skillId, Creature effector, Creature effected) {
		SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		if (skillTemplate == null) {
			LoggerFactory.getLogger(SkillEngine.class).warn("Could not apply effect, invalid skill id " + skillId + " (effector: " + effector + ")");
			return;
		}
		applyEffectDirectly(skillTemplate, skillTemplate.getLvl(), effector, effected, null);
	}

	public void applyEffectDirectly(int skillId, int lvl, Creature effector, Creature effected) {
		SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		if (skillTemplate == null) {
			LoggerFactory.getLogger(SkillEngine.class).warn("Could not apply effect, invalid skill id " + skillId + " (effector: " + effector + ")");
			return;
		}
		applyEffectDirectly(skillTemplate, lvl, effector, effected, null);
	}

	public void applyEffectDirectly(SkillTemplate skillTemplate, int skillLevel, Creature effector, Creature effected) {
		applyEffectDirectly(skillTemplate, skillLevel, effector, effected, null);
	}

	/**
	 * This method is used to apply directly effect of given skill without checking properties, sending packets, etc Should be only used from quest
	 * scripts, or when you are sure about it
	 * 
	 * @param duration
	 *          - null = calculates native duration, number = uses forced duration value (0 meaning permanent)
	 */
	public void applyEffectDirectly(SkillTemplate skillTemplate, int skillLevel, Creature effector, Creature effected, Integer duration) {
		Effect ef = new Effect(effector, effected, skillTemplate, skillLevel, duration);
		ef.setIsForcedEffect(true);
		ef.initialize();
		ef.applyEffect();
	}

	/**
	 * similar function to applyeffectdirectly, but effect is not forced that means it checks for resists etc
	 * 
	 * @param skillId
	 * @param effector
	 * @param effected
	 */
	public void applyEffect(int skillId, Creature effector, Creature effected) {
		SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		if (skillTemplate == null) {
			LoggerFactory.getLogger(SkillEngine.class).warn("Could not apply effect, invalid skill id " + skillId + " (effector: " + effector + ")");
			return;
		}
		Effect ef = new Effect(effector, effected, skillTemplate, skillTemplate.getLvl());
		ef.initialize();
		ef.applyEffect();
	}

}
