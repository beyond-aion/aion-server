package ai.instance.empyreanCrucible;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.SummonerAI;

/**
 * Administrator Arminos of the stage 6 bonus: his fight starts on the first hit as well, since he only notices players within 15 meters.
 */
@AIName("empyrean_ghost_arminos")
public class GhostArminosAI extends SummonerAI {

	private final AtomicBoolean engaged = new AtomicBoolean();

	public GhostArminosAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (engaged.compareAndSet(false, true))
			getPosition().getWorldMapInstance().getInstanceHandler().onAggro(getOwner());
	}
}
