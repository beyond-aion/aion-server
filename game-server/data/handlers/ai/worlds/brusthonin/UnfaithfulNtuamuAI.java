package ai.worlds.brusthonin;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller, Neon
 */
@AIName("unfaithfulntuamu")
public class UnfaithfulNtuamuAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(50);

	public UnfaithfulNtuamuAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		Npc ntuamu = getOwner();
		Npc vampireQueen = (Npc) spawn(214583, ntuamu.getX(), ntuamu.getY(), ntuamu.getZ(), ntuamu.getHeading());
		vampireQueen.getLifeStats().setCurrentHpPercent(phaseHpPercent);
		vampireQueen.getObserveController().attach(new ActionObserver(ObserverType.DEATH) {

			@Override
			public void died(Creature creature) {
				AIActions.scheduleRespawn(UnfaithfulNtuamuAI.this);
			}
		});
		AIActions.deleteOwner(this);
	}
}
