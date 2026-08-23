package ai.instance.empyreanCrucible;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author w4terbomb
 */
@AIName("ranger_preceptor")
public class RangerPreceptorAI extends AggressiveNpcAI {

	private final AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> task;

	public RangerPreceptorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		cancelTask();
		int msgId = (getNpcId() == 217579) ? 1500212 : 1500214;
		PacketSendUtility.broadcastMessage(getOwner(), msgId);
		super.handleDied();
	}

	@Override
	protected void handleBackHome() {
		cancelTask();
		isHome.set(true);
		super.handleBackHome();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			startSkillTask();
		}
	}

	private void startSkillTask() {
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (isDead()) {
				cancelTask();
			} else {
				startSkillEvent();
			}
		}, 10000, 30000);
	}

	private void cancelTask() {
		if (task != null && !task.isCancelled()) {
			task.cancel(true);
		}
	}

	private void startSkillEvent() {
		int msgId = (getNpcId() == 217579) ? 1500211 : 1500213;
		PacketSendUtility.broadcastMessage(getOwner(), msgId);
		getOwner().queueSkill(19601, 15, 2000, NpcSkillTargetAttribute.RANDOM);
		getOwner().queueSkill(19603, 20);
	}
}
