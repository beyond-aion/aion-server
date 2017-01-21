package ai.instance.abyssal_splinter;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;

import ai.AggressiveNpcAI;

/**
 * @author Luzien, Ritsu
 */
@AIName("dayshade")
public class DayshadeAI extends AggressiveNpcAI {

	private AtomicBoolean isHome = new AtomicBoolean(true);

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			AIActions.die(this, creature);
			spawn(216949, 455.5502f, 702.09485f, 433.13727f, (byte) 108); // ebonsoul
			spawn(216948, 447.1937f, 683.72217f, 433.1805f, (byte) 108); // rukril
			AIActions.deleteOwner(DayshadeAI.this);
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		isHome.set(true);
	}
}
