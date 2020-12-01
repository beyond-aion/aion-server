package ai.instance.nightmareCircus;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Effect;
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

	public YumeAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleDialogStart(Player player) {

	}

	@Override
	public void onEffectApplied(Effect effect) {
		switch (effect.getSkillId()) {
			case 21463, 21465 -> AIActions.useSkill(this, 21467);
		}
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature instanceof Player p) {
			if (isStart.compareAndSet(false, true)) {
				skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
					if (p.getLifeStats().getHpPercentage() < 100) {
						if (Rnd.nextBoolean()) {
							PacketSendUtility.broadcastMessage(getOwner(), 1501126);
						}
						AIActions.useSkill(YumeAI.this, 21466);
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
