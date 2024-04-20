package ai.instance.azoturanFortress;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * @author Antraxx, Neon
 */
@AIName("betrayericaronix")
public class BetrayerIcaronixAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(50);

	public BetrayerIcaronixAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		Npc icaronixTheBetrayer = (Npc) spawn(214599, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading());
		icaronixTheBetrayer.getLifeStats().setCurrentHpPercent(50);
		AIActions.deleteOwner(this);
	}
}
