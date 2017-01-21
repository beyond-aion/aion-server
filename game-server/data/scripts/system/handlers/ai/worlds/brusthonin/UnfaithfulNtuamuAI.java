package ai.worlds.brusthonin;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 * @modified Neon
 */
@AIName("unfaithfulntuamu")
public class UnfaithfulNtuamuAI extends AggressiveNpcAI {

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
			vampireQueen.getObserveController().attach(new ActionObserver(ObserverType.DEATH) {

				@Override
				public void died(Creature creature) {
					AIActions.scheduleRespawn(UnfaithfulNtuamuAI.this);
				}
			});
			AIActions.deleteOwner(this);
		}
	}
}
