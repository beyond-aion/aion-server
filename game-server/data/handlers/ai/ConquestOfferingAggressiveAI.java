package ai;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Yeats
 */
@AIName("conquest_offering_aggressive")
public class ConquestOfferingAggressiveAI extends AggressiveNpcAI {

	private Npc spawner;

	public ConquestOfferingAggressiveAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		findAndSetCreator();
	}

	private void findAndSetCreator() {
		if (getCreatorId() != 0 && getPosition().getWorldMapInstance().getObject(getCreatorId()) instanceof Npc npc)
			spawner = npc;
	}

	@Override
	public void handleDied() {
		if (spawner != null && !spawner.isDead()) {
			spawner.getAi().onCustomEvent(1); // notify spawner that npc died
			spawnRandomNpc();
		}
		super.handleDied();
	}

	// spawn a shugo or a portal
	private void spawnRandomNpc() {
		int npcId = 0;
		if (Rnd.chance() < 55) {
			if (Rnd.chance() < 45) { // spawn a shugo
				npcId = 856175 + Rnd.get(0, 3);
			} else { // spawn a portal
				npcId = getOwner().getWorldId() == 210050000 ? 833018 : 833021;
			}
		}
		if (npcId != 0)
			spawn(npcId, getOwner().getX() + 0.3f, getOwner().getY() + 0.3f, getOwner().getZ() + 0.2f, getOwner().getHeading());
	}
}
