package ai.instance.beshmundirTemple;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;

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
		for (VisibleObject object : getKnownList().getKnownObjects().values()) {
			if (object instanceof Npc && isInRange(object, 60)) {
				Npc npc = (Npc) object;
				if (!npc.getLifeStats().isAlreadyDead() && npc.getNpcId() == 216215 && (int) npc.getSpawn().getY() == (int) getSpawnTemplate().getY()) {
					npc.getAi2().onCreatureEvent(AIEventType.CREATURE_AGGRO, creature);
				}
			}
		}
	}
}
