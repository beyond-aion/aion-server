package ai.worlds.brusthonin;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Cheatkiller
 * @modified Neon
 */
@AIName("unfaithfulntuamu")
public class UnfaithfulNtuamuAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 50) {
			Npc ntuamu = getOwner();
			Npc vampireQueen = (Npc) spawn(214583, ntuamu.getX(), ntuamu.getY(), ntuamu.getZ(), ntuamu.getHeading());
			vampireQueen.getLifeStats().setCurrentHpPercent(50);
			vampireQueen.getObserveController().addObserver(new ActionObserver(ObserverType.DEATH) {

				NpcController ntuamuController = ntuamu.getController();

				@Override
				public void died(Creature creature) {
					ntuamuController.addTask(TaskId.RESPAWN, ntuamuController.scheduleRespawn());
				}

			});
			ntuamu.getController().onDelete();
		}
	}
}
