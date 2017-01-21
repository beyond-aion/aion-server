package ai.worlds;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.world.WorldPosition;

import ai.GeneralNpcAI;

/**
 * @author Tibald
 */
@AIName("namelessgrave")
public class NamelessGraveAI extends GeneralNpcAI {

	private AtomicBoolean isSpawned = new AtomicBoolean(false);

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		if (isSpawned.compareAndSet(false, true)) {
			rndSpawnInRange(283905, Rnd.get(1, 2));
			rndSpawnInRange(283905, Rnd.get(1, 2));
		}
	}

	private Npc rndSpawnInRange(int npcId, float distance) {
		float direction = Rnd.get(0, 199) / 100f;
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		WorldPosition p = getPosition();
		return (Npc) spawn(npcId, p.getX() + x1, p.getY() + y1, p.getZ(), (byte) 0);
	}
}
