package ai.worlds.eltnen;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * Spawns Harpback when Kratia dies, and schedules respawn of Kratia when Harpback dies.
 * 
 * @author Neon
 */
@AIName("kratia")
public class Kratia extends AggressiveNpcAI {

	public Kratia(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDied() {
		Npc kratia = getOwner();
		Npc harpback = (Npc) spawn(211812, kratia.getX(), kratia.getY(), kratia.getZ(), kratia.getHeading());
		harpback.getObserveController().attach(new ActionObserver(ObserverType.DEATH) {

			@Override
			public void died(Creature creature) {
				AIActions.scheduleRespawn(Kratia.this);
			}
		});
		super.handleDied();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_RESPAWN -> false;
			default -> super.ask(question);
		};
	}
}
