package ai.events;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;

import ai.OneDmgAI;

/**
 * @author Tibald, Neon
 */
@AIName("rednosedgrankerking")
public class RedNosedGrankerKingAI extends OneDmgAI {

	private AtomicBoolean addsSpawned = new AtomicBoolean(false);

	public RedNosedGrankerKingAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 50) {
			if (addsSpawned.compareAndSet(false, true)) {
				switch (getNpcId()) {
					case 219292, 219294 -> {
						int npcId = getNpcId() + 1;
						rndSpawnInRange(npcId, 1, 3);
						rndSpawnInRange(npcId, 1, 3);
						rndSpawnInRange(npcId, 1, 3);
					}
				}
			}
		}
	}

	@Override
	protected void handleBackHome() {
		addsSpawned.set(false);
		super.handleBackHome();
	}

	@Override
	protected void handleDied() {
		long chestSpawnCount = getAggroList().getList().stream()
			.filter(a -> a.getAttacker() instanceof Player && ((Player) a.getAttacker()).isOnline() && isInRange((Player) a.getAttacker(), 50)).count();
		for (int i = 0; i < chestSpawnCount; i++)
			rndSpawnInRange(701457, 1, 6); // stolen solorius box

		getOwner().getKnownList().forEachNpc(n -> {
			if (n.getNpcId() == 219293 || n.getNpcId() == 219295)
				n.getController().delete();
		});
		super.handleDied();
	}

}
