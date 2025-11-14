package ai.instance.theShugoEmperorsVault;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Yeats
 */
@AIName("idsweep_healtower")
public class IDSweep_HealTower extends GeneralNpcAI {

	private Future<?> schedule;

	public IDSweep_HealTower(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		schedule = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::tryHeal, 2000, 3000);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTask();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTask();
	}

	private void cancelTask() {
		if (schedule != null && !schedule.isCancelled()) {
			schedule.cancel(true);
		}
	}

	private void tryHeal() {
		if (!getOwner().isSpawned() || getOwner().isDead())
			return;
		if (getKnownList().streamPlayers().noneMatch(p -> !p.isDead() && !p.getLifeStats().isFullyRestoredHp() && isInRange(p, 3)))
			return;
		getOwner().setTarget(getOwner());
		SkillEngine.getInstance().getSkill(getOwner(), 21837, 1, getOwner()).useSkill();
	}
}
