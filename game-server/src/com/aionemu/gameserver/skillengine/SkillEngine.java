package com.aionemu.gameserver.skillengine;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.skillengine.effect.EffectType;
import com.aionemu.gameserver.skillengine.model.*;
import com.aionemu.gameserver.skillengine.model.Effect.ForceType;
import com.aionemu.gameserver.skillengine.properties.Properties;

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

	public ChargeSkill getChargeSkill(Creature creature, int skillId, int skillLevel, int motionId, Skill startSkill) {
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		if (template == null)
			return null;
		return new ChargeSkill(template, creature, skillLevel, motionId, startSkill);
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
	 * Applies the skill's effects to one or multiple targets, depending on its template properties.
	 *
	 * @return affected targets
	 */
	public List<Creature> applyEffectsDirectly(int skillId, Creature effector, Creature firstTarget, float x, float y, float z) {
		SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		Properties properties = skillTemplate.getProperties();
		List<Creature> targets = new ArrayList<>();
		targets.add(firstTarget);
		if (properties != null) // add valid targets in range
			properties.validateEffectedList(targets, firstTarget, effector, skillTemplate, x, y, z);
		for (Creature target : targets)
			applyEffect(effector, target, skillTemplate, skillTemplate.getLvl(), null, ForceType.DEFAULT);
		return targets;
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

	public Effect createCriticalEffect(Player attacker, Creature target, int skillId) {
		if (target.getEffectController().isUnderNormalShield())
			return null;
		if (skillId != 0) {
			SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
			if (skillTemplate.getType() == SkillType.MAGICAL) // magical skills do not stun
				return null;
			if (skillTemplate.hasAnyEffect(true, EffectType.PULLED, EffectType.STUMBLE, EffectType.STAGGER, EffectType.STUN, EffectType.BACKDASH,
				EffectType.DASH, EffectType.MOVEBEHIND, EffectType.RANDOMMOVELOC, EffectType.RECALLINSTANT)
				|| !skillTemplate.hasAnyEffect(EffectType.SKILLATKDRAININSTANT, EffectType.SKILLATTACKINSTANT))
				return null;
		}

		int id = 0;
		ItemGroup mainHandWeaponType = attacker.getEquipment().getMainHandWeaponType();
		if (mainHandWeaponType != null) {
			switch (mainHandWeaponType) {
				case POLEARM, STAFF, GREATSWORD -> id = 8218; // stumble
				case BOW -> id = 8217; // stun
			}
		}

		if (id == 0)
			return null;

		SkillTemplate skillTemplate = checkAndGetSkillTemplate(id);
		if (skillTemplate != null) {
			Effect ef = new Effect(attacker, target, skillTemplate, skillTemplate.getLvl(), null, null, true);
			ef.initialize();
			return ef;
		}
		return null;
	}

}
