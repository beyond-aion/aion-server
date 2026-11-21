package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_POSITION;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Sarynth, SVDNESS
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FearEffect")
public class FearEffect extends EffectTemplate {

	@XmlAttribute
	protected int resistchance = 100;

	@Override
	public void applyEffect(Effect effect) {
		Creature effected = effect.getEffected();
		effected.getEffectController().removeHideEffects();
		// Fear stops gliding
		if (effected instanceof Player && ((Player) effected).isInGlidingState()) {
			((Player) effected).getFlyController().onStopGliding();
		}
		effect.addToEffectedController();
	}

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, StatEnum.FEAR_RESISTANCE, null);
	}

	@Override
	public void startEffect(final Effect effect) {
		final Creature effector = effect.isReflected() ? effect.getOriginalEffected() : effect.getEffector();
		final Creature effected = effect.getEffected();
		effected.getController().cancelCurrentSkill(effector);
		effect.setAbnormal(AbnormalState.FEAR);
		effected.getEffectController().setAbnormal(AbnormalState.FEAR);
		if (effected instanceof Npc) {
			EmoteManager.emoteStartAttacking((Npc) effected, effector); // set weapon_equipped for faster walk speed
			effected.getAi().setStateIfNot(AIState.FEAR);
		}
		ForcedMovementTask.start(effect, position, new FearTask(effector, effected));
		// resistchance of fear effect to damage, if value is lower than 100, fear can be interrupted bz damage
		// example skillId: 540 Terrible howl
		if (resistchance < 100) {
			effect.addObserver(effected, new ActionObserver(ObserverType.ATTACKED) {

				@Override
				public void attacked(Creature creature, int skillId) {
					if (Rnd.chance() >= resistchance)
						effected.getEffectController().removeEffect(effect.getSkillId());
				}
			});
		}
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.FEAR);
		effect.getEffected().getMoveController().abortMove();
		PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(), new SM_POSITION(effect.getEffected()));
		if (effect.getEffected() instanceof Npc) {
			effect.getEffected().getAi().setStateIfNot(AIState.IDLE);
			effect.getEffected().getAi().onCreatureEvent(AIEventType.ATTACK, effect.getEffected());
		}
	}

	static final class FearTask extends ForcedMovementTask {

		/** Maximum heading deviation from the direct escape route, in heading units (3° each), one entry per attempt. */
		private static final byte[] PLAYER_HEADING_DEVIATIONS = { 6, 6, 6, 6 };
		/** Npcs try harder to get away, so their deviation cone widens with each attempt. */
		private static final byte[] NPC_HEADING_DEVIATIONS = { 6, 17, 28, 38, 49 };
		/** No step is taken while the target is farther away from the source of its fear than this. */
		private static final int LEASH_DISTANCE = 100;
		/** Closer than this the direction away from the source is undefined, and the target runs where the source faces. */
		private static final float MIN_ESCAPE_DISTANCE = 0.05f;

		private final Creature effector;

		FearTask(Creature effector, Creature effected) {
			super(effected);
			this.effector = effector;
		}

		@Override
		protected boolean isInControl() {
			return effected.getEffectController().isUnderFear();
		}

		@Override
		protected boolean act() {
			if (!PositionUtil.isInRange(effected, effector, LEASH_DISTANCE))
				return false;
			return step(getEscapeHeading(), effected instanceof Npc ? NPC_HEADING_DEVIATIONS : PLAYER_HEADING_DEVIATIONS);
		}

		private byte getEscapeHeading() {
			if (Math.abs(effected.getX() - effector.getX()) < MIN_ESCAPE_DISTANCE && Math.abs(effected.getY() - effector.getY()) < MIN_ESCAPE_DISTANCE)
				return effector.getHeading();
			return PositionUtil.convertAngleToHeading(PositionUtil.calculateAngleFrom(effector, effected));
		}
	}
}
