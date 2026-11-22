package com.aionemu.gameserver.skillengine.effect;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
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
		final Creature effected = effect.getEffected();
		effected.getController().cancelCurrentSkill(effect.getEffector());
		effected.getEffectController().setAbnormal(AbnormalState.CONFUSE);
		effect.setAbnormal(AbnormalState.CONFUSE);
		if (effected instanceof Player) // npcs carry on with their fight, nothing but the cast they were busy with ends
			ForcedMovementTask.start(effect, position, new ConfuseTask(effected));
	}

	@Override
	public void endEffect(Effect effect) {
		Creature effected = effect.getEffected();
		effected.getEffectController().unsetAbnormal(AbnormalState.CONFUSE);
		if (effected instanceof Player) {
			effected.getMoveController().abortMove();
			PacketSendUtility.broadcastPacketAndReceive(effected, new SM_POSITION(effected));
		}
	}

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

		/**
		 * Attacks a random living enemy in attack range, the effector included.
		 * <p>
		 * Better than retail: retail takes the first enemy in its world cell order and checks line of sight only when the hit lands, so an
		 * enemy behind a wall can be picked and the tick passes without a hit while a visible one stands next to it. Here only visible
		 * enemies are candidates and one of them is picked at random. To go back, drop the canSee filter and take the first candidate.
		 */
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
