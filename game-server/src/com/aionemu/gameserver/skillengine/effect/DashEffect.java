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
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DashEffect")
public class DashEffect extends DamageEffect {

	@Override
	public void calculate(Effect effect) {
		Creature effected = effect.getEffected();
		if (effected.equals(effect.getSkill().getFirstTarget())) { // move only once for Dash-AoE (e.g 2705)
			effect.setDashStatus(DashStatus.DASH);
			byte h = PositionUtil.getHeadingTowards(effect.getEffector(), effected);
			double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(h));
			float distance = effect.getEffector().getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide() + effected.getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide() + 1;
			final float x1 = (float) Math.cos(Math.PI + radian) * distance;
			final float y1 = (float) Math.sin(Math.PI + radian) * distance;
			Vector3f closestCollision = GeoService.getInstance().getClosestCollision(effect.getEffected(),effected.getX() + x1, effected.getY() + y1, effected.getZ());
			effect.getSkill().setTargetPosition(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), h);
			World.getInstance().updatePosition(effect.getEffector(), closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), h);
		}
		super.calculate(effect);
	}
}
