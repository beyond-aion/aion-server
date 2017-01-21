package ai.instance.RukibukiCircusTroupe;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Ritsu
 */
@AIName("yume")
public class YumeAI extends GeneralNpcAI {

	private AtomicBoolean isStart = new AtomicBoolean(false);
	private Future<?> skillTask;

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleDialogStart(Player player) {

	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature instanceof Player) {
			final Player p = (Player) creature;
			if (isStart.compareAndSet(false, true)) {
				skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

					@Override
					public void run() {
						if (p.getLifeStats().getHpPercentage() < 100) {
							PacketSendUtility.broadcastMessage(getOwner(), 1501126);
							AIActions.useSkill(YumeAI.this, 21467);
						}
					}
				}, 15000, 15000);
			}
		}
	}

	private void cancelTask() {
		if (skillTask != null && !skillTask.isCancelled()) {
			skillTask.cancel(true);
		}
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}
}
