package ai.instance.unstableSplinterpath;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * @author Luzien, Ritsu, Cheatkiller
 */
@AIName("unstabledayshade")
public class UnstableDayshadeAI extends AggressiveNpcAI {

	private final AtomicBoolean isHome = new AtomicBoolean(true);

	public UnstableDayshadeAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			AIActions.die(this, creature);
			spawn(219552, 455.5502f, 702.09485f, 433.13727f, (byte) 108); // ebonsoul
			spawn(219551, 447.1937f, 683.72217f, 433.1805f, (byte) 108); // rukril
			AIActions.deleteOwner(UnstableDayshadeAI.this);
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		isHome.set(true);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, REWARD_AP -> false;
			default -> super.ask(question);
		};
	}
}
