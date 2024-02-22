package com.aionemu.gameserver.skillengine.effect;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.*;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.controllers.attack.AttackResult;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.skillengine.change.Change;
import com.aionemu.gameserver.skillengine.condition.Conditions;
import com.aionemu.gameserver.skillengine.effect.modifier.ActionModifier;
import com.aionemu.gameserver.skillengine.effect.modifier.ActionModifiers;
import com.aionemu.gameserver.skillengine.model.*;
import com.aionemu.gameserver.utils.stats.StatFunctions;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Effect")
public abstract class EffectTemplate {

	protected ActionModifiers modifiers;
	protected List<Change> change;
	@XmlAttribute
	protected int effectid;
	@XmlAttribute(required = true)
	protected int duration2;
	@XmlAttribute
	protected int duration1;
	@XmlAttribute(name = "randomtime")
	protected int randomTime;
	@XmlAttribute(name = "e")
	protected int position;
	@XmlAttribute(name = "basiclvl")
	protected int basicLvl;
	@XmlAttribute(name = "hittype")
	protected HitType hitType = HitType.EVERYHIT;
	@XmlAttribute(name = "hittypeprob2")
	protected int hitTypeProb = 100;
	@XmlAttribute(name = "element")
	protected SkillElement element = SkillElement.NONE;
	@XmlElement(name = "subeffect")
	protected SubEffect subEffect;
	@XmlElement(name = "conditions")
	protected Conditions effectConditions;
	@XmlElement(name = "subconditions")
	protected Conditions effectSubConditions;
	@XmlAttribute(name = "hoptype")
	protected HopType hopType;
	@XmlAttribute(name = "hopa")
	protected int hopA; // effects the aggro-value (hate)
	@XmlAttribute(name = "hopb")
	protected int hopB; // effects the aggro-value (hate)
	@XmlAttribute(name = "noresist")
	protected boolean noResist;
	@XmlAttribute(name = "accmod1")
	protected int accMod1;// accdelta
	@XmlAttribute(name = "accmod2")
	protected int accMod2;// accvalue
	@XmlAttribute(name = "preeffect")
	protected int[] preEffects;
	@XmlAttribute(name = "preeffect_prob")
	protected int preEffectProb = 100;
	@XmlAttribute(name = "critprobmod2")
	protected int critProbMod2 = 100;
	@XmlAttribute(name = "critadddmg1")
	protected int critAddDmg1 = 0;
	@XmlAttribute(name = "critadddmg2")
	protected int critAddDmg2 = 0;

	@XmlAttribute
	protected int value;
	@XmlAttribute
	protected int delta;
	@XmlAttribute(name = "skill_efficiency")
	protected int skillEfficiency;
	@XmlAttribute(name = "max_damage_chance")
	protected int maxDamageChance;
	@XmlAttribute(name = "max_damage_delta")
	protected int maxDamageDelta;

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return the delta
	 */
	public int getDelta() {
		return delta;
	}

	/**
	 * @return the duration2
	 */
	public int getDuration2() {
		return duration2;
	}

	/**
	 * @return the duration1
	 */
	public int getDuration1() {
		return duration1;
	}

	/**
	 * @return the randomtime
	 */
	public int getRandomTime() {
		return randomTime;
	}

	/**
	 * @return the modifiers
	 */
	public ActionModifiers getModifiers() {
		return modifiers;
	}

	/**
	 * @return the change
	 */
	public List<Change> getChange() {
		return change;
	}

	/**
	 * @return the effectid
	 */
	public int getEffectId() {
		return effectid;
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @return the basicLvl
	 */
	public int getBasicLvl() {
		return basicLvl;
	}

	/**
	 * @return the element
	 */
	public SkillElement getElement() {
		return element;
	}

	/**
	 * @return the preEffectProb
	 */
	public int getPreEffectProb() {
		return preEffectProb;
	}

	private int[] getPreEffects() {
		return preEffects;
	}

	/**
	 * @return the critProbMod2
	 */
	public int getCritProbMod2() {
		return critProbMod2;
	}

	/**
	 * @return the critAddDmg1
	 */
	public int getCritAddDmg1() {
		return critAddDmg1;
	}

	/**
	 * @return the critAddDmg2
	 */
	public int getCritAddDmg2() {
		return critAddDmg2;
	}

	/**
	 * Gets the effect conditions status
	 * 
	 * @return list of Conditions for effect template
	 */
	public Conditions getEffectConditions() {
		return effectConditions;
	}

	/**
	 * Gets the sub effect conditions status
	 * 
	 * @return list of Conditions for sub effects within effect template
	 */
	public Conditions getEffectSubConditions() {
		return effectSubConditions;
	}

	public ActionModifier getActionModifiers(Effect effect) {
		if (modifiers == null)
			return null;

		// Only one of modifiers will be applied now
		for (ActionModifier modifier : modifiers.getActionModifiers()) {
			if (modifier.check(effect))
				return modifier;
		}

		return null;
	}

	/**
	 * @return the subEffect
	 */
	public SubEffect getSubEffect() {
		return subEffect;
	}

	/**
	 * @return the accMod1
	 */
	public int getAccMod1() {
		return accMod1;
	}

	/**
	 * @return the accMod2
	 */
	public int getAccMod2() {
		return accMod2;
	}

	/**
	 * @return the base value (damage, heal, etc.) according to the skill template, it should equal the value stated in the skill description
	 */
	protected int calculateBaseValue(Effect effect) {
		return value + delta * effect.getSkillLevel();
	}

	/**
	 * Calculate effect result
	 *
	 * @param effect
	 */
	public void calculate(Effect effect) {
		calculate(effect, null, null, element);
	}

	public boolean calculate(Effect effect, StatEnum statEnum, SpellStatus spellStatus) {
		return calculate(effect, statEnum, spellStatus, element);
	}

	/**
	 * 1) check conditions 2) check preeffect 3) check effectresistrate 4) check noresist 5) decide if its magical or physical effect 6) physical -
	 * check cannotmiss 7) check magic resist / dodge 8) addsuccess exceptions: buffbind buffsilence buffsleep buffstun randommoveloc recallinstant
	 * returneffect returnpoint shieldeffect signeteffect summoneffect xpboosteffect
	 * 
	 * @param effect
	 * @param statEnum
	 * @param spellStatus
	 */
	public boolean calculate(Effect effect, StatEnum statEnum, SpellStatus spellStatus, SkillElement element) {
		if (effect.getSkillTemplate().isPassive()) {
			this.addSuccessEffect(effect, spellStatus);
			return true;
		}

		if (statEnum != null && isAlteredState(statEnum) && isImmuneToAbnormal(effect, statEnum)) {
			return false;
		}

		// dont check for forced effect
		if (effect.isForcedEffect()) {
			this.addSuccessEffect(effect, spellStatus);
			calculateDamage(effect);
			return true;
		}

		// check conditions
		if (!effectConditionsCheck(effect))
			return false;

		if (firstEffectCheck(effect, statEnum, spellStatus, element)) {
			addSuccessEffect(effect, spellStatus);
			calculateDamage(effect);
			return true;
		} else if (nextEffectCheck(effect, spellStatus, statEnum)) {
			addSuccessEffect(effect, spellStatus);
			calculateDamage(effect);
			return true;
		} else {
			return false;
		}
	}

	private boolean firstEffectCheck(Effect effect, StatEnum statEnum, SpellStatus spellStatus, SkillElement element) {
		if (getPosition() == 1) {
			// check effectresistrate
			if (!calculateEffectResistRate(effect, statEnum)) {
				return false;
			}
			if (!noResist && !isCannotMiss()) {
				if (isDodgedOrResisted(effect)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private boolean nextEffectCheck(Effect effect, SpellStatus spellStatus, StatEnum statEnum) {
		EffectTemplate firstEffect = effect.effectInPos(1);
		if (getPosition() > 1) {
			if (Rnd.chance() < getPreEffectProb()) {
				int[] positions = getPreEffects();
				if (positions != null) {
					for (int pos : positions) {
						if (!effect.isInSuccessEffects(pos)) {
							return false;
						}
					}
				}
				if (!noResist && !isCannotMiss()) {
					if (!calculateEffectResistRate(effect, statEnum) || isDodgedOrResisted(effect)) {
						if (!(firstEffect instanceof DamageEffect)) {
							effect.getSuccessEffects().remove(firstEffect);
						}
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	private boolean isCannotMiss() {
		return this instanceof SkillAttackInstantEffect && ((SkillAttackInstantEffect) this).isCannotmiss();
	}

	private boolean isDodgedOrResisted(Effect effect) {
		// check for BOOST_RESIST
		int boostResist = 0;
		switch (effect.getSkillTemplate().getSubType()) {
			case DEBUFF:
				boostResist = effect.getEffector().getGameStats().getStat(StatEnum.BOOST_RESIST_DEBUFF, 0).getCurrent();
				break;
		}
		int accMod = accMod2 + accMod1 * effect.getSkillLevel() + effect.getAccModBoost() + boostResist;
		switch (element) {
			case NONE:
				return StatFunctions.checkIsDodgedHit(effect.getEffector(), effect.getEffected(), accMod);
			default:
				return Rnd.get(1, 1000) <= StatFunctions.calculateMagicalResistRate(effect.getEffector(), effect.getEffected(), accMod, element);
		}
	}

	private void addSuccessEffect(Effect effect, SpellStatus spellStatus) {
		effect.addSuccessEffect(this);
		if (spellStatus != null)
			effect.setSpellStatus(spellStatus);
	}

	/**
	 * Check all condition statuses for effect template
	 */
	private boolean effectConditionsCheck(Effect effect) {
		Conditions effectConditions = getEffectConditions();
		return effectConditions == null || effectConditions.validate(effect);
	}

	/**
	 * Apply effect to effected
	 * 
	 * @param effect
	 */
	public abstract void applyEffect(Effect effect);

	/**
	 * Start effect on effected
	 * 
	 * @param effect
	 */
	public void startEffect(Effect effect) {
	}

	public void calculateDamage(Effect effect) {
		// evaluate skill reflect for non-dmg skills
		AttackResult attackResult = new AttackResult(0, effect.getAttackStatus(), hitType);
		effect.getEffected().getObserveController().checkShieldStatus(Collections.singletonList(attackResult), effect, effect.getEffector(),
			ShieldType.SKILL_REFLECTOR);
		if (attackResult.getShieldType() != 0) {
			effect.setShieldDefense(effect.getShieldDefense() | attackResult.getShieldType());
			effect.setReflectedSkillId(attackResult.getReflectedSkillId());
		}
	}

	/**
	 * @param effect
	 */
	public void calculateSubEffect(Effect effect) {
		if (subEffect == null)
			return;
		ActionModifiers mod = this.getModifiers();
		if (mod != null) {
			ActionModifier modifier = this.getActionModifiers(effect);
			if (modifier == null) {
				return;
			}
		}
		// Pre-Check for sub effect conditions
		if (!effectSubConditionsCheck(effect)) {
			effect.setSubEffectAborted(true);
			return;
		}

		// chance to trigger subeffect
		if (Rnd.chance() >= subEffect.getChance())
			return;

		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(subEffect.getSkillId());
		int level = 1;
		int accBoost = effect.getAccModBoost();
		if (subEffect.isAddEffect()) { // Only used by signet bursts
			level = effect.getSignetBurstedCount();
			accBoost = Short.MAX_VALUE; // sub effects cannot be resisted by magic resist in case of signet bursts
		}
		Effect newEffect = new Effect(effect.getEffector(), effect.getOriginalEffected(), template, level, null, effect.getForceType(), true);
		newEffect.setShieldDefense(effect.getShieldDefense());
		newEffect.setAccModBoost(accBoost);
		newEffect.initialize();
		if (newEffect.getSpellStatus() != SpellStatus.DODGE && newEffect.getSpellStatus() != SpellStatus.RESIST)
			effect.setSpellStatus(newEffect.getSpellStatus());
		effect.setSubEffect(newEffect);
		effect.setSubEffectType(newEffect.getSubEffectType());
		effect.setTargetLoc(newEffect.getTargetX(), newEffect.getTargetY(), newEffect.getTargetZ());
	}

	/**
	 * Check all sub effect condition statuses for effect
	 */
	private boolean effectSubConditionsCheck(Effect effect) {
		return effectSubConditions == null || effectSubConditions.validate(effect);
	}

	@SuppressWarnings("fallthrough")
	public int calculateHate(Effect effect) {
		if (hopType != null) {
			int hate = 0;
			switch (hopType) {
				case DAMAGE:
					hate = effect.getReserveds(0).getValue();
				case SKILLLV:
					int skillLvl = effect.getSkillLevel();
					hate += hopB + hopA * skillLvl; // Aggro-value of the effect
					break;
				default:
					throw new UnsupportedOperationException("Unhandled effect type " + hopType + " for hate calculation");
			}
			return Math.max(1, hate);
		}
		return 0;
	}

	/**
	 * @param effect
	 */
	public void startSubEffect(Effect effect) {
		if (subEffect == null)
			return;
		// Apply-Check for sub effect conditions
		if (effect.isSubEffectAbortedBySubConditions())
			return;
		if (effect.getSubEffect() != null)
			effect.getSubEffect().applyEffect();
	}

	/**
	 * Do periodic effect on effected
	 * 
	 * @param effect
	 */
	public void onPeriodicAction(Effect effect) {
	}

	/**
	 * End effect on effected
	 * 
	 * @param effect
	 */
	public void endEffect(Effect effect) {
	}

	/**
	 * @param effect
	 * @param statEnum
	 * @return true = no resist, false = resisted
	 */
	public boolean calculateEffectResistRate(Effect effect, StatEnum statEnum) {
		if (statEnum == null)
			return true;

		Creature effected = effect.getEffected();
		Creature effector = effect.getEffector();

		if (effected == null || effected.getGameStats() == null || effector == null || effector.getGameStats() == null)
			return false;

		// calculate cumulative resist chance for fear, sleep and paralyze if effector & effected are players
		if (effector.getMaster() instanceof Player && effected instanceof Player) {
			if (statEnum == StatEnum.FEAR_RESISTANCE && ((Player) effected).getFearCount() >= 3
				&& ((Player) effected).validateCumulativeFearResistExpirationTime()) {
				if (Rnd.get(1, 1000) <= getCumulativeResistChanceFor(((Player) effected).getFearCount())) {
					return false;
				}
			} else if (statEnum == StatEnum.SLEEP_RESISTANCE && ((Player) effected).getSleepCount() >= 3
				&& ((Player) effected).validateCumulativeSleepResistExpirationTime()) {
				if (Rnd.get(1, 1000) <= getCumulativeResistChanceFor(((Player) effected).getSleepCount())) {
					return false;
				}
			} else if (statEnum == StatEnum.PARALYZE_RESISTANCE && ((Player) effected).getParalyzeCount() >= 3
				&& ((Player) effected).validateCumulativeParalyzeResistExpirationTime()) {
				if (Rnd.get(1, 1000) <= getCumulativeResistChanceFor(((Player) effected).getParalyzeCount())) {
					return false;
				}
			}
		}

		int effectPower = 1000;

		if (isAlteredState(statEnum))
			effectPower -= effected.getGameStats().getAbnormalResistance().getCurrent();

		// effect resistance
		effectPower -= effected.getGameStats().getStat(statEnum, 0).getCurrent();

		// penetration
		StatEnum penetrationStat = this.getPenetrationStat(statEnum);
		if (penetrationStat != null)
			effectPower += effector.getGameStats().getStat(penetrationStat, 0).getCurrent();

		// resist mod
		if (effector.isPvpTarget(effected)) { // pvp
			int lvlDiff = effected.getLevel() - effector.getLevel();
			if (lvlDiff > 4) {
				float reductionRate = 0.1f * (lvlDiff - 4); // see https://forums.aiononline.com/topic/25-arena-of-discipline-entries/?page=2#elComment_2213
				effectPower *= Math.max(1 - reductionRate, 0.1f);
			}
		} else if (effected instanceof Npc) { // resist mod PvE
			int hpGaugeMod = ((Npc) effected).getObjectTemplate().getRank().ordinal();
			effectPower -= hpGaugeMod * 100;
		}
		return Rnd.get(1, 1000) <= effectPower;
	}

	private boolean isImmuneToAbnormal(Effect effect, StatEnum statEnum) {
		Creature effected = effect.getEffected();
		if (effected != effect.getEffector()) {
			if (effected instanceof Npc || effected instanceof Summon) {
				if (effected.getAi().ask(AIQuestion.IS_IMMUNE_TO_ABNORMAL_STATES))
					return true;
				if (((NpcTemplate) effected.getObjectTemplate()).getStatsTemplate().getRunSpeed() == 0)
					return statEnum == StatEnum.PULLED_RESISTANCE || statEnum == StatEnum.STAGGER_RESISTANCE || statEnum == StatEnum.STUMBLE_RESISTANCE;
			}
		}
		return false;
	}

	/**
	 * @param stat
	 * @return true = it's an altered state effect, false = it is Poison/Bleed dot (normal Dots have statEnum null here)
	 */
	private boolean isAlteredState(StatEnum stat) {
		return switch (stat) {
			case BLEED_RESISTANCE, POISON_RESISTANCE -> false;
			default -> true;
		};
	}

	private StatEnum getPenetrationStat(StatEnum statEnum) {
		StatEnum toReturn = null;
		try {
			toReturn = StatEnum.valueOf(statEnum.toString() + "_PENETRATION");
		} catch (Exception e) {
			LoggerFactory.getLogger(EffectTemplate.class).warn("Missing statenum penetration for " + statEnum.toString());
		}
		return toReturn;
	}

	private int getCumulativeResistChanceFor(int resistCount) {
		return switch (resistCount) {
			case 0, 1, 2 -> 0;
			case 3 -> 200;
			case 4 -> 400;
			default -> 1000;
		};
	}
}
