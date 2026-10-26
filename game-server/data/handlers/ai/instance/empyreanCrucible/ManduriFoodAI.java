package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * Feed or water for the stage 5 giant. It only stands where the supply device put it out; whoever finishes it decides what the giant gets.
 */
@AIName("empyrean_manduri_food")
public class ManduriFoodAI extends NpcAI {

	private volatile Creature lastAttacker;

	public ManduriFoodAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (giant() instanceof Npc giant && giant.getAi() instanceof ManduriGiantAI ai)
			ai.onFoodSpawned(getOwner());
	}

	@Override
	protected void handleAttack(Creature creature) {
		lastAttacker = creature;
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		Npc giant = giant();
		if (giant != null && giant.getAi() instanceof ManduriGiantAI ai)
			ai.onFoodDied(getNpcId(), giant.equals(lastAttacker));
	}

	private Npc giant() {
		return getPosition().getWorldMapInstance().getNpc(217553);
	}
}
