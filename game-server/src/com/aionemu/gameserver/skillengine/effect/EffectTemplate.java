package com.aionemu.gameserver.skillengine.effect;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.change.Change;
import com.aionemu.gameserver.skillengine.condition.Conditions;
import com.aionemu.gameserver.skillengine.effect.modifier.ActionModifier;
import com.aionemu.gameserver.skillengine.effect.modifier.ActionModifiers;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HitType;
import com.aionemu.gameserver.skillengine.model.HopType;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.model.SpellStatus;
import com.aionemu.gameserver.skillengine.model.TransformType;
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
	@XmlAttribute(name = "hittype", required = false)
	protected HitType hitType = HitType.EVERYHIT;
	@XmlAttribute(name = "hittypeprob2", required = false)
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
	protected int hopA; // effects the agro-value (hate)
	@XmlAttribute(name = "hopb")
	protected int hopB; // effects the agro-value (hate)
	@XmlAttribute(name = "noresist")
	protected boolean noResist;
	@XmlAttribute(name = "accmod1")
	protected int accMod1;// accdelta
	@XmlAttribute(name = "accmod2")
	protected int accMod2;// accvalue
	@XmlAttribute(name = "preeffect")
	protected String preEffect;
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
	
	@XmlTransient
	protected EffectType effectType = null;
	
	@XmlTransient
	protected Logger log = LoggerFactory.getLogger(this.getClass());

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
	public int getEffectid() {
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
	 * @return the preEffect
	 */
	public String getPreEffect() {
		return preEffect;
	}

	/**
	 * @return the preEffectProb
	 */
	public int getPreEffectProb() {
		return preEffectProb;
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

	/**
	 * @param value
	 * @return
	 */
	public ActionModifier getActionModifiers(Effect effect) {
		if (modifiers == null)
			return null;

		/**
		 * Only one of modifiers will be applied now
		 */
		for (ActionModifier modifier : modifiers.getActionModifiers()) {
			if (modifier.check(effect))
				return modifier;
		}

		return null;
	}
	
	/**
	 * @return the effectType
	 */
	public EffectType getEffectType() {
		return effectType;
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
	 * 1) check conditions 
	 * 2) check preeffect 
	 * 3) check effectresistrate 
	 * 4) check noresist 
	 * 5) decide if its magical or physical effect 
	 * 6) physical - check cannotmiss 
	 * 7) check magic resist / dodge 
	 * 8) addsuccess
	 * 
	 * exceptions:
	 * buffbind
	 * buffsilence
	 * buffsleep
	 * buffstun
	 * randommoveloc
	 * recallinstant
	 * returneffect
	 * returnpoint
	 * shieldeffect
	 * signeteffect
	 * summoneffect
	 * xpboosteffect
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
		
		if (statEnum != null && isAlteredState(statEnum) && isImuneToAbnormal(effect, statEnum)) {
			return false;
		}
		
		//dont check for forced effect
		if (effect.getIsForcedEffect()) {
			this.addSuccessEffect(effect, spellStatus);
			calculateDamage(effect);
			return true;
		}
		
		// check conditions
		if (!effectConditionsCheck(effect))
			return false;
		
		if (firstEffectCheck(effect, statEnum, spellStatus, element)) {
	  	this.addSuccessEffect(effect, spellStatus);
			calculateDamage(effect);
			return true;
	  }
		else if (nextEffectCheck(effect, spellStatus, statEnum)) {
			this.addSuccessEffect(effect, spellStatus);
			calculateDamage(effect);
			return true;
		}
	  else {
	  	return false;
	  }
	}
	
	private boolean firstEffectCheck(Effect effect, StatEnum statEnum, SpellStatus spellStatus, SkillElement element) {
		if (this.getPosition() == 1) {
			  // check effectresistrate
				if (!this.calculateEffectResistRate(effect, statEnum)) {
					return false;
				}
				boolean cannotMiss = false;
				if (this instanceof SkillAttackInstantEffect)
					cannotMiss = ((SkillAttackInstantEffect)this).isCannotmiss();
				if (!noResist && !cannotMiss) {
					//check for BOOST_RESIST
					int boostResist = 0;
					switch (effect.getSkillTemplate().getSubType()) {
						case DEBUFF:
							boostResist = effect.getEffector().getGameStats().getStat(StatEnum.BOOST_RESIST_DEBUFF, 0).getCurrent();
							break;
					}
					int accMod = accMod2 + accMod1 * effect.getSkillLevel() + effect.getAccModBoost() + boostResist;
					switch (element) {
						case NONE:
							if (StatFunctions.calculatePhysicalDodgeRate(effect.getEffector(), effect.getEffected(), accMod))
								return false;
							break;
						default:
							if (Rnd.get(0, 1000) < StatFunctions.calculateMagicalResistRate(effect.getEffector(), effect.getEffected(), accMod))
								return false;
							break;
					}
				}
				return true;
			}
		return false;
	}
	
	private boolean nextEffectCheck(Effect effect, SpellStatus spellStatus, StatEnum statEnum) {
		EffectTemplate firstEffect = effect.effectInPos(1);
		if (this.getPosition() > 1) {
			if (Rnd.get(0, 100) < this.getPreEffectProb()) {
				FastList<Integer> positions = getPreEffects();
				int successCount = 0;
				for (int pos : positions) {
					if (effect.isInSuccessEffects(pos)) {
						successCount++;
					}
				}
				if (successCount == positions.size()) {
					boolean cannotMiss = false;
					if (this instanceof SkillAttackInstantEffect)
						cannotMiss = ((SkillAttackInstantEffect)this).isCannotmiss();
					if (!noResist && !cannotMiss) {
						if (!this.calculateEffectResistRate(effect, statEnum)) {
							if (!(firstEffect instanceof DamageEffect)) {
								effect.getSuccessEffect().remove(firstEffect);
							}
							return false;
						}
						//check for BOOST_RESIST
						int boostResist = 0;
						switch (effect.getSkillTemplate().getSubType()) {
							case DEBUFF:
								boostResist = effect.getEffector().getGameStats().getStat(StatEnum.BOOST_RESIST_DEBUFF, 0).getCurrent();
								break;
						}
						int accMod = accMod2 + accMod1 * effect.getSkillLevel() + effect.getAccModBoost() + boostResist;
						switch (element) {
							case NONE:
								if (StatFunctions.calculatePhysicalDodgeRate(effect.getEffector(), effect.getEffected(), accMod))
									return false;
								break;
							default:
								if (Rnd.get(0, 1000) < StatFunctions.calculateMagicalResistRate(effect.getEffector(), effect.getEffected(), accMod))
									return false;
								break;
						}
					}
					return true;
				}
			}
		}
		return false;
	}
	
	
		

	private void addSuccessEffect(Effect effect, SpellStatus spellStatus) {
		effect.addSucessEffect(this);
		if (spellStatus != null)
			effect.setSpellStatus(spellStatus);
	}

	/**
	 * Check all condition statuses for effect template
	 */
	private boolean effectConditionsCheck(Effect effect) {
		Conditions effectConditions = getEffectConditions();
		return effectConditions != null ? effectConditions.validate(effect) : true;
	}

	private FastList<Integer> getPreEffects() {
		FastList<Integer> preEffects = new FastList<Integer>();
		
		if (this.getPreEffect() == null)
			return preEffects;
		
		String[] parts = this.getPreEffect().split("_");
		for (String part : parts) {
			preEffects.add(Integer.parseInt(part));
		}

		return preEffects;
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
	};
	
  public void calculateDamage(Effect effect) {
		
	};

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
		if (Rnd.get(100) > subEffect.getChance())
			return;

		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(subEffect.getSkillId());
		int level = 1;
		if (subEffect.isAddEffect())
			level = effect.getSignetBurstedCount();
		Effect newEffect = new Effect(effect.getEffector(), effect.getEffected(), template, level, 0);
		newEffect.setAccModBoost(effect.getAccModBoost());
		newEffect.setSubEffect(true);
		newEffect.initialize();
		if (newEffect.getSpellStatus() != SpellStatus.DODGE && newEffect.getSpellStatus() != SpellStatus.RESIST)
			effect.setSpellStatus(newEffect.getSpellStatus());
		effect.setSubEffect(newEffect);
		effect.setSkillMoveType(newEffect.getSkillMoveType());
		effect.setTargetLoc(newEffect.getTargetX(), newEffect.getTargetY(), newEffect.getTargetZ());
	}

	/**
	 * Check all sub effect condition statuses for effect
	 */
	private boolean effectSubConditionsCheck(Effect effect) {
		return effectSubConditions != null ? effectSubConditions.validate(effect) : true;
	}

	/**
	 * Hate will be added to result value only if particular effect template has success result
	 * 
	 * @param effect
	 */
	public void calculateHate(Effect effect) {
		if (hopType == null)
			return;

		if (effect.getSuccessEffect().isEmpty())
			return;

		int currentHate = effect.getEffectHate();
		if (hopType != null) {
			switch (hopType) {
				case DAMAGE:
					currentHate += effect.getReserveds(0).getValue();
					break;
				case SKILLLV:
					int skillLvl = effect.getSkillLevel();
					currentHate += hopB + hopA * skillLvl; // Agro-value of the effect
				default:
					break;
			}
		}
		if (currentHate == 0)
			currentHate = 1;
		effect.setEffectHate(StatFunctions.calculateHate(effect.getEffector(), currentHate));
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
		if (effect.getEffected() == null || effect.getEffected().getGameStats() == null
				|| effect.getEffector() == null || effect.getEffector().getGameStats() == null)
				return false;
		
		Creature effected = effect.getEffected();
		Creature effector = effect.getEffector();

		if (statEnum == null)
			return true;
		
		if (effect.getSkillTemplate().getGroup() != null && effect.getSkillTemplate().getGroup().equals("WA_AVENGINGCRASH")) {
			return true;
		}
		
		int effectPower = 1000;
		
		if (isAlteredState(statEnum)) {
			effectPower -= effect.getEffected().getGameStats().getStat(StatEnum.ABNORMAL_RESISTANCE_ALL, 0).getCurrent();
		}
		
		// effect resistance
		effectPower -= effect.getEffected().getGameStats().getStat(statEnum, 0).getCurrent();
		
		// penetration
		StatEnum penetrationStat = this.getPenetrationStat(statEnum);
		if (penetrationStat != null)
			effectPower += effector.getGameStats().getStat(penetrationStat, 0).getCurrent();
		
		// resist mod pvp
		if (effector.isPvpTarget(effect.getEffected())) {
			int differ = (effected.getLevel() - effector.getLevel());
			if (differ > 2 && differ < 8)
				effectPower -= Math.round((effectPower * (differ - 2) / 15f));
			else if (differ >= 8)
				effectPower *= 0.1f;
		}
		
		// resist mod PvE
		if (effect.getEffected() instanceof Npc) {
			Npc effectrd = (Npc) effect.getEffected();
			int hpGaugeMod = effectrd.getObjectTemplate().getRank().ordinal() - 1;
			effectPower -= hpGaugeMod * 100;
		}
		
		return Rnd.get(1000) <= effectPower;
	}
	
	private boolean isImuneToAbnormal(Effect effect, StatEnum statEnum) {
		Creature effected = effect.getEffected();
		if (effected != effect.getEffector()) {
			if (effected instanceof Npc) {
				Npc npc = (Npc) effected;
				if (npc.isBoss() || npc.hasStatic() || npc instanceof Kisk
					|| npc.getAi2().ask(AIQuestion.CAN_RESIST_ABNORMAL).isPositive())
					return true;
				if (npc.getObjectTemplate().getStatsTemplate().getRunSpeed() == 0) {
					if (statEnum == StatEnum.PULLED_RESISTANCE
							|| statEnum == StatEnum.STAGGER_RESISTANCE
							|| statEnum == StatEnum.STUMBLE_RESISTANCE)
						return true;
				}
			}
			if(effected.getTransformModel().getType() == TransformType.AVATAR) {
				if (statEnum == StatEnum.SLOW_RESISTANCE)
				return true;
			}
		}
		return false;
	}
	/**
	 * @param statEnum
	 * @return true = it's an altered state effect, false = it is Poison/Bleed dot (normal Dots have statEnum null here)
	 */
	private boolean isAlteredState(StatEnum stat) {
		switch (stat) {
			case BLEED_RESISTANCE:
			case POISON_RESISTANCE:
				return false;
		}
		return true;
	}

	private StatEnum getPenetrationStat(StatEnum statEnum) {
		StatEnum toReturn = null;
		try {
			toReturn = StatEnum.valueOf(statEnum.toString() + "_PENETRATION");
		}
		catch (Exception e) {
			log.info("[WARN]missing statenum penetration for " + statEnum.toString());
		}
		return toReturn;
	}

	void afterUnmarshal(Unmarshaller u, Object parent) {
		EffectType temp = null;
		try {
			temp = EffectType.valueOf(this.getClass().getName().replaceAll("com.aionemu.gameserver.skillengine.effect.", "").replaceAll("Effect", "").toUpperCase());
		} catch (Exception e) {
			log.info("missing effectype for "+this.getClass().getName().replaceAll("com.aionemu.gameserver.skillengine.effect.", "").replaceAll("Effect", "").toUpperCase());
		}
		
		this.effectType = temp;
	}
}
