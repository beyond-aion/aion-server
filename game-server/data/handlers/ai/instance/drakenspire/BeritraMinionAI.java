package ai.instance.drakenspire;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNoLootNpcAI;

/**
 * @author Estrayl
 */
@AIName("drakenspire_beritra_minion")
public class BeritraMinionAI extends AggressiveNoLootNpcAI {

	private final AtomicInteger followAttempts = new AtomicInteger();
	private Future<?> deathTask;

	public BeritraMinionAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(this::aggroPlayer, 500);
		deathTask = ThreadPoolManager.getInstance().schedule(() -> {
			int npcId = getSkillNpcId();
			if (npcId != 0)
				spawn(npcId, getPosition().getX(), getPosition().getY(), getPosition().getZ(), (byte) 0);
			ThreadPoolManager.getInstance().schedule(() -> AIActions.die(this), 500);
		}, 20000);
	}

	private void aggroPlayer() {
		getKnownList().getKnownPlayers().values().stream().filter(p -> !p.isDead() && PositionUtil.isInRange(p, 152.38f, 518.68f, 1749.6f, 24)).findAny()
			.ifPresent(p -> getAggroList().addHate(p, 10000));
	}

	private int getSkillNpcId() {
		return switch (getNpcId()) {
			case 855444 -> 855447;
			case 855445 -> 855448;
			case 855446 -> 855449;
			default -> 0;
		};
	}

	private boolean isTargetInsideArena() {
		return PositionUtil.isInRange(getTarget(), 151.9f, 518.6f, 1749.6f, 26);
	}

	@Override
	protected void handleTargetTooFar() {
		if (!isTargetInsideArena()) {
			if (followAttempts.incrementAndGet() < 3) {
				ThreadPoolManager.getInstance().schedule(this::handleTargetTooFar, 2000);
			} else {
				followAttempts.set(0);
				getAggroList().stopHating(getTarget());
				Creature mostHated = getAggroList().getMostHated();
				if (mostHated != null)
					onCreatureEvent(AIEventType.TARGET_CHANGED, mostHated);
				else
					onGeneralEvent(AIEventType.TARGET_GIVEUP);
			}
			return;
		}
		super.handleTargetTooFar();
	}

	@Override
	protected void handleDespawned() {
		if (deathTask != null && !deathTask.isDone())
			deathTask.cancel(true);
		super.handleDespawned();
	}
}
