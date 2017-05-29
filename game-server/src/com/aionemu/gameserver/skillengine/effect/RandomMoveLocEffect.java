package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.DashStatus;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.SkillMoveType;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Bio
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RandomMoveLocEffect")
public class RandomMoveLocEffect extends EffectTemplate {

	@XmlAttribute(name = "distance")
	private float distance;
	@XmlAttribute(name = "direction")
	private float direction;
	@XmlAttribute(name = "reserved5")
	private int reserved5;

	@Override
	public void applyEffect(Effect effect) {
		final Player effector = (Player) effect.getEffector();

		Skill skill = effect.getSkill();
		World.getInstance().updatePosition(effector, skill.getX(), skill.getY(), skill.getZ(), skill.getH());
	}

	@Override
	public void calculate(Effect effect) {
		effect.addSucessEffect(this);
		DashStatus ds = reserved5 == 1 ? DashStatus.RANDOMMOVELOC_NEW : DashStatus.RANDOMMOVELOC;
		SkillMoveType mt = direction == 1 ? SkillMoveType.MOVEBEHIND : SkillMoveType.DODGE;
		effect.setSkillMoveType(mt);
		effect.setDashStatus(ds);
		final Player effector = (Player) effect.getEffector();
		// Move Effector backwards direction=1 or frontwards direction=0
		if (distance != 0) {
			double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(effector.getHeading()));
			float x1 = (float) (Math.cos(Math.PI * direction + radian) * distance);
			float y1 = (float) (Math.sin(Math.PI * direction + radian) * distance);
			float targetZ = GeoService.getInstance().getZ(effector.getWorldId(), effector.getX() + x1, effector.getY() + y1, effector.getZ() + 1.5f,
				effector.getInstanceId());
			if (Float.isNaN(targetZ))
				targetZ = effector.getZ() + 1.5f;
			byte intentions = (byte) (CollisionIntention.PHYSICAL.getId() | CollisionIntention.DOOR.getId());
			Vector3f closestCollision = GeoService.getInstance().getClosestCollision(effector, effector.getX() + x1, effector.getY() + y1, targetZ, false,
				intentions);
			effect.getSkill().setTargetPosition(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), effector.getHeading());
		} else
			effect.getSkill().setTargetPosition(effector.getX(), effector.getY(), effector.getZ(), effector.getHeading());
	}
}
