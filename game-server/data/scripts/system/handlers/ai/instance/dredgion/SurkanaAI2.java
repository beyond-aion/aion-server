package ai.instance.dredgion;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Creature;

import ai.OneDmgNoActionAI2;

/**
 * recieve only 1 dmg with each attack(handled by super) Aggro the whole room on attack
 * 
 * @author Luzien
 */
@AIName("surkana")
public class SurkanaAI2 extends OneDmgNoActionAI2 {

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		// roomaggro
		checkForSupport(creature);
	}

	private void checkForSupport(Creature creature) {
		getKnownList().forEachNpc(npc -> {
			if (isInRange(npc, 25) && !npc.getLifeStats().isAlreadyDead())
				npc.getAi2().onCreatureEvent(AIEventType.CREATURE_AGGRO, creature);
		});
	}
}
