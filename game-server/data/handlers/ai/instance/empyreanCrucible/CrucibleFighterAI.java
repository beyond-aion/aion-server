package ai.instance.empyreanCrucible;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * Arena fighter whose fight the instance also starts on the first hit, not only when the fighter notices a player itself: much stronger
 * players are never aggroed, and a player may strike first from outside the aggro range.
 */
@AIName("empyrean_fighter")
public class CrucibleFighterAI extends AggressiveNpcAI {

	private final AtomicBoolean engaged = new AtomicBoolean();

	public CrucibleFighterAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (engaged.compareAndSet(false, true))
			getPosition().getWorldMapInstance().getInstanceHandler().onAggro(getOwner());
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		engaged.set(false); // a new fight counts its time again
	}
}
