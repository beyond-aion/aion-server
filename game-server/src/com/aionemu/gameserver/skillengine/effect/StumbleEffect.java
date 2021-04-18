package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SubEffectType;
import com.aionemu.gameserver.skillengine.model.SpellStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StumbleEffect")
public class StumbleEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void startEffect(Effect effect) {
		final Creature effected = effect.getEffected();
		effected.getController().cancelCurrentSkill(effect.getEffector());
		effected.getEffectController().removeParalyzeEffects();
		effected.getEffectController().removeStunEffects();
		if (effected instanceof Player player) {
			player.getFlyController().onStopGliding();
			player.getController().onStopMove();
		}
		World.getInstance().updatePosition(effected, effect.getTargetX(), effect.getTargetY(), effect.getTargetZ(), effected.getHeading());
		// TODO: FI_RobustCrash_G1 or FI_Whirlwind_G1 don't send anything, find pattern
		if (effected instanceof Player)
			PacketSendUtility.broadcastPacketAndReceive(effected, new SM_FORCED_MOVE(effect.getEffector(), effected.getObjectId(),
					effect.getTargetX(), effect.getTargetY(), effect.getTargetZ()));
		effect.getEffected().getEffectController().setAbnormal(AbnormalState.STUMBLE);
		effect.setAbnormal(AbnormalState.STUMBLE);
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
		if (effect.isSubEffect() && !(effect.getEffected() instanceof Player))
			effect.setSubEffectType(SubEffectType.STUMBLE);
		final Creature effector = effect.getEffector();
		final Creature effected = effect.getEffected();
		double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(PositionUtil.getHeadingTowards(effector, effect.getEffected())));
		float x1 = (float) (Math.cos(radian) * 2);
		float y1 = (float) (Math.sin(radian) * 2);
		Vector3f closestCollision = GeoService.getInstance().getClosestCollision(effected, effected.getX() + x1, effected.getY() + y1, effected.getZ());
		effect.setTargetLoc(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ());
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.STUMBLE);
	}
}
