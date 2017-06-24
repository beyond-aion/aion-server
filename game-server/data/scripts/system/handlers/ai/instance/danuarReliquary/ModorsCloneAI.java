package ai.instance.danuarReliquary;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("modors_clone")
public class ModorsCloneAI extends AggressiveNpcAI {

	private Future<?> skillTask;
	private AtomicBoolean isHome = new AtomicBoolean(true);

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getOwner().getNpcId() != 284384) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					PacketSendUtility.broadcastMessage(getOwner(), 343532);
				}
			}, Rnd.get(5000, 10000));
		}
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return ItemAttackType.MAGICAL_WATER;
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			startSkillTask();
		}
	}

	private void startSkillTask() {
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isDead()) {
					cancelSkillTask();
				} else {
					if (!isDead()) {
						Creature creature = getAggroList().getMostHated();
						SkillEngine.getInstance().getSkill(getOwner(), 21175, 60, creature).useSkill();
					}
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (!isDead()) {
								Creature creature = getAggroList().getMostHated();
								SkillEngine.getInstance().getSkill(getOwner(), 21175, 60, creature).useSkill();
							}
						}

					}, 15000);
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (!isDead()) {
								SkillEngine.getInstance().getSkill(getOwner(), 21177, 1, getOwner()).useSkill();
								if (getOwner().getNpcId() != 284384)
									spawn(284386, 255.98627f, 259.0136f, 241.73842f, (byte) 0);
							}
						}
					}, 25000);
				}
			}

		}, 5000, 40000);
	}

	private void cancelSkillTask() {
		if (skillTask != null && !skillTask.isDone()) {
			skillTask.cancel(true);
		}
	}

	@Override
	protected void handleDied() {
		cancelSkillTask();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelSkillTask();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		cancelSkillTask();
		isHome.set(true);
	}
}
