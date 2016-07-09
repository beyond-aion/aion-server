package ai.instance.azoturanFortress;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI2;

/**
 * @author Antraxx
 * @modified Neon
 */
@AIName("betrayericaronix")
public class BetrayerIcaronixAI2 extends AggressiveNpcAI2 {

	private AtomicBoolean isStartEvent = new AtomicBoolean();

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 50 && isStartEvent.compareAndSet(false, true)) { // icaronix transforms at 50% hp
			Npc icaronixTheBetrayer = (Npc) spawn(214599, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading());
			icaronixTheBetrayer.getLifeStats().setCurrentHpPercent(50);
			AI2Actions.deleteOwner(this);
		}
	}
}
