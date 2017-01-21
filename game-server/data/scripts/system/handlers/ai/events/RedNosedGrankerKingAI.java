package ai.events;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;

import ai.OneDmgAI;

/**
 * @author Tibald, Neon
 */
@AIName("rednosedgrankerking")
public class RedNosedGrankerKingAI extends OneDmgAI {

	private AtomicBoolean addsSpawned = new AtomicBoolean(false);

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 50) {
			if (addsSpawned.compareAndSet(false, true)) {
				switch (getNpcId()) {
					case 219292:
					case 219294:
						int npcId = getNpcId() + 1;
						rndSpawnInRange(npcId, 3);
						rndSpawnInRange(npcId, 3);
						rndSpawnInRange(npcId, 3);
						break;
				}
			}
		}
	}

	private Npc rndSpawnInRange(int npcId, float maxDistance) {
		float distance = 1 + Rnd.nextFloat() * (maxDistance - 1);
		double directionRadian = Math.toRadians(Rnd.get(360));
		WorldPosition p = getPosition();
		float x = p.getX() + (float) (Math.cos(directionRadian) * distance);
		float y = p.getY() + (float) (Math.sin(directionRadian) * distance);
		float z = GeoService.getInstance().getZ(p.getMapId(), x, y, p.getZ(), 0, p.getInstanceId());
		return (Npc) spawn(npcId, x, y, z, (byte) Rnd.get(120));
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
			rndSpawnInRange(701457, 6); // stolen solorius box
		super.handleDied();
	}

}
