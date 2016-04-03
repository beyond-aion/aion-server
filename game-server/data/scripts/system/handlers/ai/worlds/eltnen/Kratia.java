package ai.worlds.eltnen;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

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
		harpback.getObserveController().addObserver(new ActionObserver(ObserverType.DEATH) {

			NpcController kratiaController = kratia.getController();

			@Override
			public void died(Creature creature) {
				kratiaController.addTask(TaskId.RESPAWN, kratiaController.scheduleRespawn());
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
