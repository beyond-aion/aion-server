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
@XmlType(name = "DispelDebuffEffect")
public class DispelDebuffEffect extends AbstractDispelEffect {

	@Override
	public void applyEffect(Effect effect) {
		super.applyEffect(effect, DispelCategoryType.ALL, SkillTargetSlot.DEBUFF);
	}
}
