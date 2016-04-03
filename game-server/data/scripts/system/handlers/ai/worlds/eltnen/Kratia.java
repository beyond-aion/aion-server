package ai.worlds.eltnen;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI2;

/**
 * Spawns Harpback when Kratia dies, and schedules respawn of Kratia when Harpback dies.
 * 
 * @author Neon
 */
@AIName("kratia")
public class Kratia extends AggressiveNpcAI2 {

	@Override
	protected void handleDied() {
		Npc kratia = getOwner();
		Npc harpback = (Npc) spawn(211812, kratia.getX(), kratia.getY(), kratia.getZ(), kratia.getHeading());
		harpback.getObserveController().attach(new ActionObserver(ObserverType.DEATH) {

			@Override
			public void died(Creature creature) {
				AI2Actions.scheduleRespawn(Kratia.this);
			}
		});
		super.handleDied();
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_RESPAWN:
				return false;
			default:
				return super.ask(question);
		}
	}
}
