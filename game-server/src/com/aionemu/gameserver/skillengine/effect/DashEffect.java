package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.model.DashStatus;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DashEffect")
public class DashEffect extends DamageEffect {

	@Override
	public void calculate(Effect effect) {
		Creature effected = effect.getEffected();
		if (effected.equals(effect.getSkill().getFirstTarget())) { //move only once for Dash-AoE (e.g 2705)
			effect.setDashStatus(DashStatus.DASH);
			effect.getSkill().setTargetPosition(effected.getX(), effected.getY(), effected.getZ(), effected.getHeading());
			World.getInstance().updatePosition(effect.getEffector(), effected.getX(), effected.getY(), effected.getZ(), effect.getEffector().getHeading());
		}
		super.calculate(effect);
	}
}
