package ai.instance.theShugoEmperorsVault;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.PlayerLifeStats;
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
		schedule = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::checkForHeal, 2000, 3000);
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

	/**
	 * checks if there are any wounded players nearby and heals 1x
	 */
	protected void checkForHeal() {
		if (getOwner().isSpawned() && !getOwner().isDead()) {
			for (Player player : getOwner().getKnownList().getKnownPlayers().values()) {
				PlayerLifeStats stats = player.getLifeStats();
				if (!stats.isDead() && isInRange(player, 3) && stats.getCurrentHp() < stats.getMaxHp()) {
					doHeal();
					break;
				}
			}
		}
	}

	private void cancelTask() {
		if (schedule != null && !schedule.isCancelled()) {
			schedule.cancel(true);
		}
	}

	private void doHeal() {
		getOwner().setTarget(getOwner());
		SkillEngine.getInstance().getSkill(getOwner(), 21837, 1, getOwner()).useSkill();
	}
}
