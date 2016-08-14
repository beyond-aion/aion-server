package ai.instance.beshmundirTemple;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Creature;

import ai.AggressiveNpcAI2;

/**
 * @author xTz
 */
@AIName("monolithicambusher")
public class MonolithicAmbusherAI2 extends AggressiveNpcAI2 {

	private boolean hasHelped;

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		hasHelped = false;
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		if (!hasHelped) {
			hasHelped = true;
			help(creature);
		}
	}

	private void help(Creature creature) {
		getKnownList().forEachNpc(npc -> {
			if (isInRange(npc, 60)) {
				if (!npc.getLifeStats().isAlreadyDead() && npc.getNpcId() == 216215 && (int) npc.getSpawn().getY() == (int) getSpawnTemplate().getY()) {
					npc.getAi2().onCreatureEvent(AIEventType.CREATURE_AGGRO, creature);
				}
			}
		});
	}
}
