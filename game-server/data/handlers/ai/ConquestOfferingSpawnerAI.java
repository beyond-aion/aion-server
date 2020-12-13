package ai;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * this ai handles (re-)spawns of random npc with the title "conquest offering"
 * 
 * @author Yeats
 */
@AIName("conquest_offering_spawner")
public class ConquestOfferingSpawnerAI extends NpcAI {

	private Future<?> respawnTask;

	public ConquestOfferingSpawnerAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		spawnRandomNpc();
	}

	private void spawnRandomNpc() {
		switch (getNpcId()) {
			// Inggison
			case 856150:
			case 856156:
				spawn(236307, 236335);
				break;
			case 856151:
			case 856157:
				spawn(236311, 236339);
				break;
			case 856152:
			case 856158:
				spawn(236315, 236343);
				break;
			case 856153:
			case 856159:
				spawn(236319, 236347);
				break;
			case 856154:
			case 856160:
				spawn(236323, 236351);
				break;
			case 856155:
			case 856161:
				spawn(236327, 236355);
				break;
			// Gelkmaros
			case 856162:
			case 856168:
				spawn(236363, 236391);
				break;
			case 856163:
			case 856169:
				spawn(236367, 236395);
				break;
			case 856164:
			case 856170:
				spawn(236371, 236399);
				break;
			case 856165:
			case 856171:
				spawn(236375, 236403);
				break;
			case 856166:
			case 856172:
				spawn(236379, 236407);
				break;
			case 856167:
			case 856173:
				spawn(236383, 236411);
				break;
		}
	}

	// Ncsoft calls them "normal" and "party"
	private void spawn(int startNormal, int startParty) {
		int npcId;
		// calculate what kind of npc will be spawned 'normal'(70%) and 'party' (30%)
		if (Rnd.chance() < 70) {
			// theres another kind of spawn called 'all'(30%)
			if (Rnd.chance() < 30) {
				npcId = getRndNpc(startNormal);
			} else {
				npcId = startNormal + Rnd.get(0, 3);
			}
		} else {
			// theres another kind of spawn called 'all'(30%)
			if (Rnd.chance() < 30) {
				npcId = getRndNpc(startNormal);
			} else {
				npcId = startParty + Rnd.get(0, 3);
			}
		}

		if (npcId != 0)
			spawn(npcId, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading());
	}

	private int getRndNpc(int curId) {
		if (curId <= 236327) { // normal
			return (236331 + Rnd.get(0, 3));
		} else if (curId <= 236355) { // party
			return (236359 + Rnd.get(0, 3));
		} else if (curId <= 236383) { // normal
			return (236387 + Rnd.get(0, 3));
		} else if (curId <= 236411) { // party
			return (236415 + Rnd.get(0, 3));
		}
		return 0;
	}

	@Override
	protected void handleCustomEvent(int eventId, Object... args) {
		if (eventId == 1) { // spawned npc died, schedule respawn
			if (respawnTask != null && !respawnTask.isCancelled() && !respawnTask.isDone())
				return;
			long respawnDelay = 600000 + (Rnd.get(0, 2) * 300000L); // random 10, 15 or 20 minutes
			respawnTask = ThreadPoolManager.getInstance().schedule(this::spawnRandomNpc, respawnDelay);
		}
	}

	@Override
	public void handleDied() {
		super.handleDied();
		cancelTask();
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	private void cancelTask() {
		if (respawnTask != null && !respawnTask.isCancelled())
			respawnTask.cancel(false);
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return 0;
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		return 0;
	}
}
