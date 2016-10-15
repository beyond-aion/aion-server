package ai.classNpc;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.knownlist.Visitor;

import ai.AggressiveNpcAI2;

/**
 * @author Cheatkiller
 */
@AIName("drakanmedic")
public class DrakanMedicAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (Rnd.get(1, 100) < 3) {
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
		getOwner().getKnownList().forEachNpc(new Visitor<Npc>() {

			@Override
			public void visit(Npc object) {
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
