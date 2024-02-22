package ai.instance.unstableSplinterpath;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Luzien, Cheatkiller
 */
@AIName("unstablepazuzu")
public class UnstablePazuzuAI extends AggressiveNpcAI {

	private final AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> task;

	public UnstablePazuzuAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			PacketSendUtility.broadcastMessage(getOwner(), 342219);
			startTask();
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		cancelTask();
		isHome.set(true);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTask();
		PacketSendUtility.broadcastMessage(getOwner(), 1500003);
	}

	private void startTask() {
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			SkillEngine.getInstance().getSkill(getOwner(), 19145, 55, getOwner()).useNoAnimationSkill();
			if (getPosition().getWorldMapInstance().getNpc(283206) == null) {
				spawn(283206, 651.351990f, 326.425995f, 465.523987f, (byte) 8);
				spawn(283206, 666.604980f, 314.497009f, 465.394012f, (byte) 27);
				spawn(283206, 685.588989f, 342.955994f, 465.908997f, (byte) 68);
				spawn(283206, 651.322021f, 346.554993f, 465.563995f, (byte) 111);
			}
		}, 5000, 70000);
	}

	private void cancelTask() {
		if (task != null && !task.isCancelled()) {
			task.cancel(true);
		}
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, REWARD_AP -> false;
			default -> super.ask(question);
		};
	}

}
