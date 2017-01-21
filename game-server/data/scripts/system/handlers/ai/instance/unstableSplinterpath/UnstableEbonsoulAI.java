package ai.instance.unstableSplinterpath;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu, Luzien
 * @edit Cheatkiller
 */
@AIName("unstableebonsoul")
public class UnstableEbonsoulAI extends AggressiveNpcAI {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> skillTask;

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
		regen();
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 95 && isHome.compareAndSet(true, false)) {
			startSkillTask();
		}
	}

	private void startSkillTask() {
		final Npc rukril = getPosition().getWorldMapInstance().getNpc(219551);
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead())
					cancelTask();
				else {
					if (getPosition().getWorldMapInstance().getNpc(283205) == null) {
						SkillEngine.getInstance().getSkill(getOwner(), 19159, 55, getOwner()).useNoAnimationSkill();
						spawn(283205, getOwner().getX() + 2, getOwner().getY() - 2, getOwner().getZ(), (byte) 0);
					}
					if (rukril != null && !rukril.getLifeStats().isAlreadyDead()) {
						SkillEngine.getInstance().getSkill(rukril, 19266, 55, rukril).useNoAnimationSkill();
						spawn(283204, rukril.getX() + 2, rukril.getY() - 2, rukril.getZ(), (byte) 0);
					}
				}
			}
		}, 5000, 70000); // re-check delay
	}

	private void cancelTask() {
		if (skillTask != null && !skillTask.isCancelled()) {
			skillTask.cancel(true);
		}
	}

	private void regen() {
		Npc rukril = getPosition().getWorldMapInstance().getNpc(219551);
		if (rukril != null && !rukril.getLifeStats().isAlreadyDead() && MathUtil.isIn3dRange(getOwner(), rukril, 5))
			if (!getOwner().getLifeStats().isFullyRestoredHp())
				getOwner().getLifeStats().increaseHp(TYPE.HP, 10000, 0, LOG.REGULAR);

	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTask();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		cancelTask();
		isHome.set(true);
		getEffectController().removeEffect(19266);
	}
}
