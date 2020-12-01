package ai.instance.azoturanFortress;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * @author Antraxx
 * @modified Neon
 */
@AIName("betrayericaronix")
public class BetrayerIcaronixAI extends AggressiveNpcAI {

	private AtomicBoolean isStartEvent = new AtomicBoolean();

	public BetrayerIcaronixAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 50 && isStartEvent.compareAndSet(false, true)) { // icaronix transforms at 50% hp
			Npc icaronixTheBetrayer = (Npc) spawn(214599, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading());
			icaronixTheBetrayer.getLifeStats().setCurrentHpPercent(50);
			AIActions.deleteOwner(this);
		}
	}
}
