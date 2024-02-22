package ai.instance.abyssal_splinter;

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
 * @author Luzien
 */
@AIName("pazuzu")
public class PazuzuAI extends AggressiveNpcAI {

	private final AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> task;

	public PazuzuAI(Npc owner) {
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
			SkillEngine.getInstance().getSkill(getOwner(), 19145, 55, getOwner());
			if (getPosition().getWorldMapInstance().getNpc(281909) == null) {
				Npc worms = (Npc) spawn(281909, 651.351990f, 326.425995f, 465.523987f, (byte) 8);
				spawn(281909, 666.604980f, 314.497009f, 465.394012f, (byte) 27);
				spawn(281909, 685.588989f, 342.955994f, 465.908997f, (byte) 68);
				spawn(281909, 651.322021f, 346.554993f, 465.563995f, (byte) 111);
				spawn(281909, 666.7373f, 314.2235f, 465.38953f, (byte) 30);

				SkillEngine.getInstance().getSkill(worms, 19291, 55, getOwner());
			}
		}, 0, 70000);
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
