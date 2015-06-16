package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.DispelCategoryType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DispelDebuffPhysicalEffect")
public class DispelDebuffPhysicalEffect extends AbstractDispelEffect {

	@Override
	public void applyEffect(Effect effect) {
		super.applyEffect(effect, DispelCategoryType.DEBUFF_PHYSICAL, SkillTargetSlot.DEBUFF);
	}
}
