package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.DispelCategoryType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractDispelEffect")
public abstract class AbstractDispelEffect extends EffectTemplate {

	@XmlAttribute
	protected int dpower;
	@XmlAttribute
	protected int power;
	@XmlAttribute(name = "dispel_level")
	protected int dispelLevel;

	public void applyEffect(Effect effect, DispelCategoryType type, SkillTargetSlot slot) {
		int count = calculateBaseValue(effect);
		int finalPower = power + dpower * effect.getSkillLevel();

		effect.getEffected().getEffectController().removeEffectByDispelCat(type, slot, count, dispelLevel, finalPower);
	}
}
