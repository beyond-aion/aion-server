package ai.instance.tiamatStrongHold;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("brigadegeneralchantra")
public class BrigadeGeneralChantraAI extends AggressiveNpcAI {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> trapTask;
	private boolean isFinalBuff;

	public BrigadeGeneralChantraAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false))
			startSkillTask();
		if (!isFinalBuff && getOwner().getLifeStats().getHpPercentage() <= 25) {
			isFinalBuff = true;
			AIActions.useSkill(this, 20942);
		}
	}

	private void startSkillTask() {
		trapTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isDead())
					cancelTask();
				else {
					startTrapEvent();
				}
			}
		}, 5000, 40000);
	}

	private void cancelTask() {
		if (trapTask != null && !trapTask.isCancelled()) {
			trapTask.cancel(true);
		}
	}

	private void startTrapEvent() {
		int[] trapNpcs = { 283092, 283094 };// 4.0
		final int trap = Rnd.get(trapNpcs);
		if (getPosition().getWorldMapInstance().getNpc(trap) == null) {
			spawn(trap, 1031.1f, 466.38f, 445.45f, (byte) 0);
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					Npc ring = getPosition().getWorldMapInstance().getNpc(trap);
					if (trap == 283092)// 4.0
						spawn(283171, 1031.1f, 466.38f, 445.45f, (byte) 0);// 4.0
					else
						spawn(283172, 1031.1f, 466.38f, 445.45f, (byte) 0);// 4.0
					ring.getController().delete();
				}
			}, 5000);
		}
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTask();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTask();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		cancelTask();
		isFinalBuff = false;
		isHome.set(true);
	}
}
