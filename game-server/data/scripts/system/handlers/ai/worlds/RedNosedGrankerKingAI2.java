package ai.worlds;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Tibald
 */
@AIName("rednosedgrankerking")
public class RedNosedGrankerKingAI2 extends AggressiveNpcAI2 {

	private AtomicBoolean isSpawned = new AtomicBoolean(false);

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 50) {
			if (isSpawned.compareAndSet(false, true)) {
				int npcId;
				switch (getNpcId()) {
					case 219292:
						npcId = getNpcId() + 1;
						rndSpawnInRange(npcId, Rnd.get(1, 2));
						rndSpawnInRange(npcId, Rnd.get(1, 2));
						rndSpawnInRange(npcId, Rnd.get(1, 2));
						break;
					case 219294:
						npcId = getNpcId() + 1;
						rndSpawnInRange(npcId, Rnd.get(1, 2));
						rndSpawnInRange(npcId, Rnd.get(1, 2));
						rndSpawnInRange(npcId, Rnd.get(1, 2));
						break;
				}
			}
		}
	}

	private Npc rndSpawnInRange(int npcId, float distance) {
		float direction = Rnd.get(0, 199) / 100f;
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		WorldPosition p = getPosition();
		return (Npc) spawn(npcId, p.getX() + x1, p.getY() + y1, p.getZ(), (byte) 0);
	}

	@Override
	protected void handleBackHome() {
		isSpawned.set(false);
		super.handleBackHome();
	}

	private void spawnChests(int npcId) {
		rndSpawnInRange(npcId, Rnd.get(1, 6));
		rndSpawnInRange(npcId, Rnd.get(1, 6));
		rndSpawnInRange(npcId, Rnd.get(1, 6));
		rndSpawnInRange(npcId, Rnd.get(1, 6));
		rndSpawnInRange(npcId, Rnd.get(1, 6));
	}

	@Override
	protected void handleDied() {
		switch (getNpcId()) {
			case 219292:
				spawnChests(701457);
				break;
			case 219294:
				spawnChests(701457);
				break;
		}
		super.handleDied();
	}

	@Override
	public int modifyOwnerDamage(int damage) {
		return 1;
	}

	@Override
	public int modifyDamage(Creature creature, int damage) {
		return 1;
	}

}
