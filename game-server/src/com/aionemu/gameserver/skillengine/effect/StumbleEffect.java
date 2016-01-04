package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillMoveType;
import com.aionemu.gameserver.skillengine.model.SpellStatus;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;
import com.jme3.math.Vector3f;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StumbleEffect")
public class StumbleEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		final Creature effector = effect.getEffector();
		final Creature effected = effect.getEffected();
		double radian = Math.toRadians(MathUtil.convertHeadingToDegree(effector.getHeading()));
		float x1 = (float) (Math.cos(radian) * 1.5f);
		float y1 = (float) (Math.sin(radian) * 1.5f);
		float z = effected.getZ();
		byte intentions = (byte) (CollisionIntention.PHYSICAL.getId() | CollisionIntention.DOOR.getId());
		Vector3f closestCollision = GeoService.getInstance().getClosestCollision(effected, effected.getX() + x1, effected.getY() + y1, z,
			false, intentions);
		float zAfterColl = closestCollision.z;
		x1 = closestCollision.x;
		y1 = closestCollision.y;
		if (Math.abs(z - zAfterColl) > 0.1f && !effected.getMoveController().isJumping()) {
			x1 = effected.getX();
			y1 = effected.getY();
			zAfterColl = z;
		}
		effect.setTargetLoc(x1, y1, zAfterColl);
		effect.addToEffectedController();
	}

	@Override
	public void startEffect(Effect effect) {
		final Creature effected = effect.getEffected();
		effected.getController().cancelCurrentSkill();
		if (effected instanceof Player)
			((Player) effected).getFlyController().onStopGliding();
		effected.getEffectController().removeParalyzeEffects();
		effected.getEffectController().removeStunEffects();
		// effected.getMoveController().abortMove();
		World.getInstance().updatePosition(effected, effect.getTargetX(), effect.getTargetY(), effect.getTargetZ(), effected.getHeading());
		PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(), new SM_FORCED_MOVE(effect.getEffector(), effect.getEffected().getObjectId(),
			effect.getTargetX(), effect.getTargetY(), effect.getTargetZ()));
		effect.getEffected().getEffectController().setAbnormal(AbnormalState.STUMBLE.getId());
		effect.setAbnormal(AbnormalState.STUMBLE.getId());
	}

	@Override
	public void calculate(Effect effect) {
		if (effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.OPENAERIAL)
			|| effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.PULLED)
			|| effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.STUMBLE)
			|| effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.STAGGER)) {
			return;
		}

		if (!super.calculate(effect, StatEnum.STUMBLE_RESISTANCE, SpellStatus.STUMBLE))
			return;
		effect.setSkillMoveType(SkillMoveType.STUMBLE);
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.STUMBLE.getId());
	}
}
