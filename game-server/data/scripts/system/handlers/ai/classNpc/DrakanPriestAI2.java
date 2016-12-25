package ai.classNpc;

import java.util.function.Consumer;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.AggressiveNpcAI2;

/**
 * @author Cheatkiller
 */
@AIName("xdrakanpriest")
public class DrakanPriestAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (Rnd.chance() < 3) {
			spawnServant();
		}
	}

	private void spawnServant() {
		Npc healServant = getPosition().getWorldMapInstance().getNpc(282988);
		if (healServant == null) {
			rndSpawn(282988, Rnd.get(1, 3));
			PacketSendUtility.broadcastMessage(getOwner(), 341784);
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		despawnServant();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		despawnServant();
	}

	private void despawnServant() {
		getOwner().getKnownList().forEachNpc(new Consumer<Npc>() {

			@Override
			public void accept(Npc object) {
				Npc healServant = getPosition().getWorldMapInstance().getNpc(282988);
				if (healServant != null)
					healServant.getController().delete();
			}
		});
	}

	private void rndSpawn(int npcId, int count) {
		for (int i = 0; i < count; i++) {
			SpawnTemplate template = rndSpawnInRange(npcId);
			VisibleObjectSpawner.spawnEnemyServant(template, getOwner().getInstanceId(), getOwner(), getOwner().getLevel());
		}
	}

	private SpawnTemplate rndSpawnInRange(int npcId) {
		float direction = Rnd.get(0, 199) / 100f;
		float x1 = (float) (Math.cos(Math.PI * direction) * 5);
		float y1 = (float) (Math.sin(Math.PI * direction) * 5);
		return SpawnEngine.addNewSingleTimeSpawn(getPosition().getMapId(), npcId, getPosition().getX() + x1, getPosition().getY() + y1, getPosition()
			.getZ(), getPosition().getHeading());
	}
}
