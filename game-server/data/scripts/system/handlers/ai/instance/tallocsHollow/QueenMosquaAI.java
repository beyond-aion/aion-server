package ai.instance.tallocsHollow;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.SummonerAI;

/**
 * @author xTz
 */
@AIName("queenmosqua")
public class QueenMosquaAI extends SummonerAI {

	private final AtomicBoolean isHome = new AtomicBoolean(true);

	public QueenMosquaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		if (isHome.compareAndSet(true,false))
			getPosition().getWorldMapInstance().setDoorState(7, false);
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		isHome.set(true);
		getPosition().getWorldMapInstance().setDoorState(7, true);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		getPosition().getWorldMapInstance().setDoorState(7, true);
	}

}
