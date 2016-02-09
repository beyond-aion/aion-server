package ai.worlds.eltnen;

import ai.OneDmgPerHitAI2;

import com.aionemu.commons.utils.Rnd;
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
 * Spawns Chaos Dracus after the Mysterious Crate dies, and schedules Crate respawn after Dracus dies.
 * 
 * @author Neon
 */
@AIName("dracusbox")
public class DracusBox extends OneDmgPerHitAI2 {

	private static int dracusId = 211800;

	@Override
	protected void handleDied() {
		int spawnId;
		switch (Rnd.get(1, 3)) {
			case 1:
				spawnId = 211792; // elroco
				break;
			case 2:
				spawnId = 211799; // oozing clodworm
				break;
			default:
				spawnId = dracusId; // chaos dracus
				break;
		}

		Npc mysteriousCrate = getOwner();
		Npc spawn = (Npc) spawn(spawnId, mysteriousCrate.getX(), mysteriousCrate.getY(), mysteriousCrate.getZ(), mysteriousCrate.getHeading());
		NpcController controller = mysteriousCrate.getController();
		controller.onDelete(); // delete the huge box instantly so we can see the spawned mob
		if (spawn.getNpcId() == dracusId) {
			spawn.getObserveController().addObserver(new ActionObserver(ObserverType.DEATH) {

				NpcController mysteriousCrateController = controller;

				@Override
				public void died(Creature creature) {
					mysteriousCrateController.addTask(TaskId.RESPAWN, mysteriousCrateController.scheduleRespawn());
				}
			});
		} else {
			controller.addTask(TaskId.RESPAWN, controller.scheduleRespawn());
		}
	}

	@Override
	protected AIAnswer pollInstance(AIQuestion question) {
		return question == AIQuestion.SHOULD_RESPAWN ? AIAnswers.NEGATIVE : super.pollInstance(question);
	}
}
