package ai.instance.shugoImperialTomb;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PositionUtil;

import ai.GeneralNpcAI;

/**
 * @author Ritsu, Estrayl
 */
@AIName("shugo_tomb_attacker")
public class ShugoTombAttackerAI extends GeneralNpcAI {

	private final static List<Integer> npc_ids = List.of(831251, 831250, 831304, 831305, 831130);
	private final AtomicBoolean isWalkerComplete = new AtomicBoolean();
	private boolean canThink = true;

	public ShugoTombAttackerAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	protected void handleSpawned() {
		canThink = false;
		super.handleSpawned();
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getMoveController().getCurrentStep().isLastStep() && isDestinationReached() && isWalkerComplete.compareAndSet(false, true)) {
			getSpawnTemplate().setWalkerId(null);
			WalkManager.stopWalking(this);
			canThink = true;
			handleHate();
		}
	}

	protected void handleHate() {
		EmoteManager.emoteStopAttacking(getOwner());
		List<Npc> towers = new ArrayList<>(
			npc_ids.stream().flatMap(id -> getOwner().getPosition().getWorldMapInstance().getNpcs(id).stream())
							.filter(npc -> npc != null && !npc.isDead() && PositionUtil.isInRange(getOwner(), npc, 30)).toList());

		if (towers.isEmpty())
			AIActions.deleteOwner(this);

		towers.sort(Comparator.comparingDouble(npc -> PositionUtil.getDistance(getOwner(), npc)));

		int hateToAdd = 1000000;
		for (Npc tower : towers) {
			getOwner().getAggroList().addHate(tower, hateToAdd);
			hateToAdd -= 50000;
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case IS_IMMUNE_TO_ABNORMAL_STATES, ALLOW_DECAY, REWARD_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
