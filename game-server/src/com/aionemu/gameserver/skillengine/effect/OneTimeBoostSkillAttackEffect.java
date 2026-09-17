package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.OneTimeBoostSkillAttack;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OneTimeBoostSkillAttackEffect")
public class OneTimeBoostSkillAttackEffect extends BufEffect {

	@XmlAttribute
	private int count;
	@XmlAttribute(name = "count_delta")
	private int countDelta;
	@XmlAttribute(name = "dmg_flat")
	private int dmgFlat;
	@XmlAttribute(name = "dmg_flat_delta")
	private int dmgFlatDelta;
	@XmlAttribute(name = "acc_flat")
	private int accFlat;
	@XmlAttribute(name = "acc_flat_delta")
	private int accFlatDelta;
	@XmlAttribute(name = "acc_percent")
	private int accPercent;
	@XmlAttribute(name = "acc_percent_delta")
	private int accPercentDelta;
	@XmlAttribute
	private SkillType type;

	@Override
	public void startEffect(Effect effect) {
		super.startEffect(effect);
		if (type == null || type == SkillType.NONE)
			return;

		int skillLvl = effect.getSkillLevel();
		OneTimeBoostSkillAttack boost = new OneTimeBoostSkillAttack(effect, type, calculateBaseValue(effect), dmgFlat + dmgFlatDelta * skillLvl,
			accPercent + accPercentDelta * skillLvl, accFlat + accFlatDelta * skillLvl, count + countDelta * skillLvl);
		effect.getEffected().getObserveController().setOneTimeBoostSkillAttack(boost);
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getObserveController().removeOneTimeBoostSkillAttack(effect);
	}

	/**
	 * @return the boostable attack type of the given effect, null if it cannot be boosted at all
	 */
	public static SkillType getBoostedSkillType(EffectTemplate template) {
		if (template instanceof SkillAttackInstantEffect || template instanceof SkillAtkDrainInstantEffect || template instanceof DashEffect
			|| template instanceof BackDashEffect || template instanceof MoveBehindEffect || template instanceof CarveSignetEffect)
			return SkillType.PHYSICAL;
		if (template instanceof SpellAttackInstantEffect || template instanceof DelayedSpellAttackInstantEffect
			|| template instanceof SpellAtkDrainInstantEffect)
			return SkillType.MAGICAL;
		return null;
	}

	public static OneTimeBoostSkillAttack getActiveBoost(Creature attacker, EffectTemplate template) {
		SkillType attackType = getBoostedSkillType(template);
		return attackType == null ? null : attacker.getObserveController().getOneTimeBoostSkillAttack(attackType);
	}
}
