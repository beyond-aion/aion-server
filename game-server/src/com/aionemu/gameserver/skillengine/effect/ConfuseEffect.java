package com.aionemu.gameserver.skillengine.effect;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_POSITION;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Yeats, SVDNESS
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConfuseEffect")
public class ConfuseEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		Creature effected = effect.getEffected();
		effected.getEffectController().removeHideEffects();
		effect.addToEffectedController();
	}

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, StatEnum.CONFUSE_RESISTANCE, null);
	}

	@Override
	public void startEffect(Effect effect) {
		final Creature effector = effect.isReflected() ? effect.getOriginalEffected() : effect.getEffector();
		final Creature effected = effect.getEffected();
		effected.getController().cancelCurrentSkill(effect.getEffector());
		effected.getEffectController().setAbnormal(AbnormalState.CONFUSE);
		effect.setAbnormal(AbnormalState.CONFUSE);
		if (effected instanceof Npc) {
			EmoteManager.emoteStartAttacking((Npc) effected, effector);
			effected.getAi().setStateIfNot(AIState.CONFUSE);
		}
		ForcedMovementTask.start(effect, position, new ConfuseTask(effected));
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.CONFUSE);
		effect.getEffected().getMoveController().abortMove();
		PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(), new SM_POSITION(effect.getEffected()));
		if (effect.getEffected() instanceof Npc) {
			effect.getEffected().getAi().setStateIfNot(AIState.IDLE);
			effect.getEffected().getAi().onCreatureEvent(AIEventType.ATTACK, effect.getEffected());
		}
	}

	/**
	 * Npcs are confused the same way as players. Retail only does this to players and leaves a confused npc alone, so it carries on with its fight as
	 * if nothing happened, which looks like a hole rather than a design. To go back, start no task for npcs.
	 */
	static final class ConfuseTask extends ForcedMovementTask {

		/** A confused creature stands still, staggers off in a random direction or attacks whatever is next to it, with about equal chances. */
		private static final int STAND_CHANCE = 33;
		private static final int MOVE_CHANCE = 34;
		/** Maximum heading deviation from the direction the creature faces, in heading units (3° each), one entry per attempt. */
		private static final byte[] HEADING_DEVIATIONS = { 30, 30, 30, 30 };

		ConfuseTask(Creature effected) {
			super(effected);
		}

		@Override
		protected boolean isInControl() {
			return effected.getEffectController().isConfused();
		}

		@Override
		protected boolean act() {
			int roll = Rnd.get(0, 100);
			if (roll < STAND_CHANCE)
				return false;
			if (roll < STAND_CHANCE + MOVE_CHANCE)
				return step(effected.getHeading(), HEADING_DEVIATIONS);
			attackAnyoneInReach();
			return false;
		}

		private void attackAnyoneInReach() {
			if (effected.isDead() || effected.isCasting())
				return;
			float attackRange = effected.getGameStats().getAttackRange().getCurrent() / 1000f;
			List<Creature> targets = new ArrayList<>();
			effected.getKnownList().forEachObject(object -> {
				if (object instanceof Creature creature && !creature.isDead() && effected.isEnemy(creature)
					&& PositionUtil.isInAttackRange(effected, creature, attackRange) && GeoService.getInstance().canSee(effected, creature))
					targets.add(creature);
			});
			if (targets.isEmpty())
				return;
			Creature target = Rnd.get(targets);
			effected.setTarget(target);
			effected.getController().attackTarget(target, 0, true);
		}
	}
}
