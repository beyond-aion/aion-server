package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.NpcObjectType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonTotemEffect")
public class SummonTotemEffect extends SummonServantEffect {

	@Override
	public void applyEffect(Effect effect) {
		Creature effector = effect.getEffector();
		float x = effector.getX();
		float y = effector.getY();
		float z = effector.getZ();
		if (effect.getSkill().isFirstTargetSelf()) {
			Creature effected = effect.getEffected();
			double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(effect.getEffector().getHeading()));
			x = effected.getX() + (float) (Math.cos(radian) * 2);
			y = effected.getY() + (float) (Math.sin(radian) * 2);
			z = effected.getZ();
		} else if (effect.getSkill().isPointSkill()) { //fix for [657]Battle Banner
			x = effect.getX();
			y = effect.getY();
			z = effect.getZ();
			if (x == 0 && y == 0) {
				x = effector.getX();
				y = effector.getY();
				z = effector.getZ();
			}
		}
		switch (effect.getSkillId()) {
			//this needs to be tested tho! Taunting Spirit should stay 20s but the client says only 15s
			case 3906:
			case 3907:
			case 3908:
			case 3909:
			case 3910:
			case 3911:
				time = 20;
				break;
			//same here Battle Banner 7s -> 15s
			case 657:
			case 658:
			case 659:
			case 660:
			case 661:
			case 662:
				time = 15;
				break;
		}
		spawnServant(effect, time, NpcObjectType.TOTEM, x, y, z);
	}

}
