package ai.classNpc;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Servant;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("xdrakanpriest")
public class DrakanPriestAI extends AggressiveNpcAI {

	public DrakanPriestAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (Rnd.chance() < 3) {
			spawnServants(282988,  Rnd.get(1, 3));
		}
	}

	void spawnServants(int npcId, int count) {
		final List<Servant> servants = findServants();
		if (servants.isEmpty()) {
			rndSpawn(npcId, count);
			PacketSendUtility.broadcastMessage(getOwner(), 341784);
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		despawnServants();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		despawnServants();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		despawnServants();
	}

	private void despawnServants() {
		findServants().forEach(servant -> servant.getController().deleteIfAliveOrCancelRespawn());
	}

	private List<Servant> findServants() {
		List<Servant> servants = new ArrayList<>();
		getPosition().getWorldMapInstance().forEachNpc(npc -> {
			if (npc instanceof Servant servant && getOwner().equals(servant.getCreator())) {
				servants.add(servant);
			}
		});
		return servants;
	}

	private void rndSpawn(int npcId, int count) {
		for (int i = 0; i < count; i++) {
			SpawnTemplate template = rndSpawnInRange(npcId);
			VisibleObjectSpawner.spawnEnemyServant(template, getOwner().getInstanceId(), getOwner(), getOwner().getLevel());
		}
	}

	private SpawnTemplate rndSpawnInRange(int npcId) {
		double angleRadians = Math.toRadians(Rnd.nextFloat(360f));
		float x1 = (float) (Math.cos(angleRadians) * 5);
		float y1 = (float) (Math.sin(angleRadians) * 5);
		return SpawnEngine.newSingleTimeSpawn(getPosition().getMapId(), npcId, getPosition().getX() + x1, getPosition().getY() + y1, getPosition().getZ(),
			getPosition().getHeading());
	}
}
