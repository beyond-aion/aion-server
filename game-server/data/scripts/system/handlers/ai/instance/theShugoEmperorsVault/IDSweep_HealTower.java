package ai.instance.theShugoEmperorsVault;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.PlayerLifeStats;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Yeats
 *
 */
@AIName("idsweep_healtower")
public class IDSweep_HealTower extends GeneralNpcAI {

	private Future<?> schedule;
	private Long startTime;
	
	@Override
	public void handleSpawned() {
		super.handleSpawned();
		startTime = System.currentTimeMillis();
		startSchedule();
	}
	
	private void startSchedule() {
		schedule = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				checkForHeal();
				
				if ((System.currentTimeMillis() - startTime) > 60000) {
					cancelTask();
				}
			}
		}, 2000, 3000);
	}

	/**
	 * checks if there are any wounded players nearby and heals 1x
	 */
	protected void checkForHeal() {
		if (getOwner().isSpawned() && !getOwner().getLifeStats().isAlreadyDead()) {
			for (Player player : getOwner().getKnownList().getKnownPlayers().values()) {
				PlayerLifeStats stats = player.getLifeStats();
				if (!stats.isAlreadyDead() && isInRange(player, 3) && stats.getCurrentHp() < stats.getMaxHp()) {
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
		
		getOwner().getController().delete();
	}
	
	private void doHeal() {
		getOwner().setTarget(getOwner());
		SkillEngine.getInstance().getSkill(getOwner(), 21837, 1, getOwner()).useSkill();
	}
}