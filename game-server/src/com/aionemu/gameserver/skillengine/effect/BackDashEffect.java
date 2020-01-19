package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.geoEngine.collision.IgnoreProperties;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.DashStatus;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BackDashEffect")
public class BackDashEffect extends DamageEffect {

	@XmlAttribute(name = "distance")
	private float distance;

	@Override
	public void calculate(Effect effect) {
		effect.setDashStatus(DashStatus.BACKDASH);
		Creature effector = effect.getEffector();
		float inverseAngle = PositionUtil.convertHeadingToAngle(effector.getHeading()) + 180; // flip by 180 degrees for opposite direction
		double radian = Math.toRadians(inverseAngle);
		float x1 = (float) (Math.cos(radian) * distance);
		float y1 = (float) (Math.sin(radian) * distance);
		Vector3f closestCollision;
		if (effector instanceof Player) {
			WorldPosition pos = ((Player) effector).getMoveController().getLastPositionFromClient();
			WorldPosition calcPos = effector.getPosition();
			Vector3f collisionPoint = GeoService.getInstance().getClosestCollision(new Vector3f(pos.getX(), pos.getY(), pos.getZ()), calcPos.getMapId(), calcPos.getInstanceId(), calcPos.getX(), calcPos.getY(), calcPos.getZ(), IgnoreProperties.of(Race.ANY));
			closestCollision = GeoService.getInstance().getClosestCollision(collisionPoint, effector.getWorldId(), effector.getInstanceId(), collisionPoint.getX() + x1, collisionPoint.getY() + y1, collisionPoint.getZ(), IgnoreProperties.of(effector.getRace()));
		} else {
			closestCollision = GeoService.getInstance().getClosestCollision(effector, effector.getX() + x1, effector.getY() + y1, effector.getZ(), IgnoreProperties.of(effector.getRace()));
		}
		effect.getSkill().setTargetPosition(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), effector.getHeading());
		World.getInstance().updatePosition(effector, closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), effector.getHeading());
		super.calculate(effect);
	}
}
