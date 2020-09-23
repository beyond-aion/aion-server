package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOVE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillMoveType;
import com.aionemu.gameserver.skillengine.model.SpellStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenAerialEffect")
public class OpenAerialEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void calculate(Effect effect) {
		if (effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.PULLED)
			|| effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.STUMBLE)
			|| effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.OPENAERIAL)
			|| effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.STAGGER)
			|| effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.SPIN))
			return;

		if(!super.calculate(effect, StatEnum.OPENAERIAL_RESISTANCE, SpellStatus.OPENAERIAL))
			return;
		effect.setSkillMoveType(SkillMoveType.STUMBLE_OR_OPENAERIAL);
		float z = effect.getEffected().getZ();
		if (!effect.getEffected().isFlying()) {
			float geoZ = GeoService.getInstance().getZ(effect.getEffected().getWorldId(),effect.getEffected().getX(), effect.getEffected().getY(), effect.getEffected().getZ() + 2, effect.getEffected().getZ() - 1, effect.getEffected().getInstanceId());
			if (!Float.isNaN(geoZ)) {
				z = geoZ;
			}
		}
		effect.setTargetLoc(effect.getEffected().getX(), effect.getEffected().getY(), z);
	}

	@Override
	public void startEffect(Effect effect) {
		final Creature effected = effect.getEffected();
		effected.getController().cancelCurrentSkill(effect.getEffector());
		effect.getEffected().getEffectController().removeParalyzeEffects();
		if (effected instanceof Player)
			((Player) effected).getFlyController().onStopGliding();
		effected.getEffectController().setAbnormal(AbnormalState.OPENAERIAL);
		effect.setAbnormal(AbnormalState.OPENAERIAL);
		World.getInstance().updatePosition(effected, effect.getTargetX(), effect.getTargetY(), effect.getTargetZ(), effected.getHeading());
		PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(), new SM_FORCED_MOVE(effect.getEffector(), effect.getEffected().getObjectId(),
				effect.getTargetX(), effect.getTargetY(), effect.getTargetZ()));
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.OPENAERIAL);
	}
}
