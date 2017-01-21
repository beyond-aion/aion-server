package ai.classNpc;

import java.util.function.Consumer;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("drakanmedic")
public class DrakanMedicAI extends AggressiveNpcAI {

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (Rnd.chance() < 3) {
			spawnServant();
		}
	}

	private void spawnServant() {
		int servant = getOwner().getObjectTemplate().getRating() == NpcRating.NORMAL ? 281621 : 281839;
		Npc holyServant = getPosition().getWorldMapInstance().getNpc(servant);
		if (holyServant == null) {
			rndSpawn(servant);
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
				int servant = getOwner().getObjectTemplate().getRating() == NpcRating.NORMAL ? 281621 : 281839;
				Npc holyServant = getPosition().getWorldMapInstance().getNpc(servant);
				if (holyServant != null)
					holyServant.getController().delete();
			}
		});
	}

	private void rndSpawn(int npcId) {
		SpawnTemplate template = rndSpawnInRange(npcId);
		VisibleObjectSpawner.spawnEnemyServant(template, getOwner().getInstanceId(), getOwner(), getOwner().getLevel());
	}

	private SpawnTemplate rndSpawnInRange(int npcId) {
		float direction = Rnd.get(0, 199) / 100f;
		float x1 = (float) (Math.cos(Math.PI * direction) * 5);
		float y1 = (float) (Math.sin(Math.PI * direction) * 5);
		return SpawnEngine.addNewSingleTimeSpawn(getPosition().getMapId(), npcId, getPosition().getX() + x1, getPosition().getY() + y1, getPosition()
			.getZ(), getPosition().getHeading());
	}
}
