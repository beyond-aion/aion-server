package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.model.DashStatus;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Sarynth, Bobobear
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MoveBehindEffect")
public class MoveBehindEffect extends DamageEffect {

	@Override
	public void calculate(Effect effect) {
		effect.setDashStatus(DashStatus.MOVEBEHIND);
		final Creature effector = effect.getEffector();
		final Creature effected = effect.getEffected();
		double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(effected.getHeading()));
		float distance = effector.getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide() + effected.getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide() + 1;
		float x1 = (float) Math.cos(Math.PI + radian) * distance;
		float y1 = (float) Math.sin(Math.PI + radian) * distance;
		Vector3f closestCollision = GeoService.getInstance().getClosestCollision(effector, effected.getX() + x1, effected.getY() + y1, effected.getZ());
		byte h = PositionUtil.getHeadingTowards(effector, effected);
		World.getInstance().updatePosition(effector, closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), h);
		// set target position for SM_CASTSPELL_RESULT
		effect.getSkill().setTargetPosition(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), h);
		super.calculate(effect);
	}
}
