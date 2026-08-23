package ai.instance.empyreanCrucible;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
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
@AIName("stigma_boss_preceptor")
public class StigmaBossPreceptorAI extends AggressiveNpcAI {

	private final AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> task;

	public StigmaBossPreceptorAI(Npc owner) {
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
		int msgId = (getNpcId() == 217586) ? 1500225 : 1500228;
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
		}, 10000, 25000);
	}

	private void cancelTask() {
		if (task != null && !task.isCancelled()) {
			task.cancel(true);
		}
	}

	private void startSkillEvent() {
		boolean isMiriya = (getNpcId() == 217586);
		if (Rnd.nextBoolean()) {
			int msgId = isMiriya ? 1500223 : 1500226;
			PacketSendUtility.broadcastMessage(getOwner(), msgId);
			getOwner().queueSkill(19615, 15, 2000, NpcSkillTargetAttribute.RANDOM);
		} else {
			int msgId = isMiriya ? 1500224 : 1500227;
			PacketSendUtility.broadcastMessage(getOwner(), msgId);
			getOwner().queueSkill(19618, 15, 2000, NpcSkillTargetAttribute.RANDOM);
		}
	}
}
