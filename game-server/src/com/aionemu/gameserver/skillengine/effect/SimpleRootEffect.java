package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_POSITION;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SpellStatus;
import com.aionemu.gameserver.skillengine.model.SubEffectType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author VladimirZ, Cheatkiller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleRootEffect")
public class SimpleRootEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void calculate(Effect effect) {
		if (effect.getEffected().getEffectController().isInAnyAbnormalState(AbnormalState.CANT_MOVE_STATE))
			return;
		if (super.calculate(effect, StatEnum.STAGGER_RESISTANCE, null) && effect.isSubEffect()) {
			effect.setSubEffectType(SubEffectType.SIMPLE_MOVE_BACK);
			final Creature effected = effect.getEffected();
			byte heading = PositionUtil.getHeadingTowards(effect.getEffector(), effect.getEffected());
			double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(heading));
			float x1 = (float) (Math.cos(radian) * 0.7f);
			float y1 = (float) (Math.sin(radian) * 0.7f);
			Vector3f closestCollision = GeoService.getInstance().getClosestCollision(effected, effected.getX() + x1, effected.getY() + y1, effected.getZ());
			effect.setTargetLoc(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ());
		}
	}

	@Override
	public void startEffect(final Effect effect) {
		final Creature effected = effect.getEffected();
		effect.setSpellStatus(SpellStatus.NONE);
		if (effected instanceof Player player)
			player.getController().onStopMove();
		if (effect.isSubEffect()) {
			World.getInstance().updatePosition(effected, effect.getTargetX(), effect.getTargetY(), effect.getTargetZ(), effected.getHeading(), false);
			if (!(effected instanceof Player))
				PacketSendUtility.broadcastPacket(effected, new SM_POSITION(effected));
		}
		effect.getEffected().getEffectController().setAbnormal(AbnormalState.SIMPLE_MOVE_BACK);
		effect.setAbnormal(AbnormalState.SIMPLE_MOVE_BACK);
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.SIMPLE_MOVE_BACK);
	}
}
