package ai.instance.drakenspire;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI2;

/**
 * @author Estrayl
 */
@AIName("wave_attacker")
public class WaveAttackerAI2 extends AggressiveNpcAI2 {
	
	@Override
	public void handleCreatureDetected(Creature creature) {
		if (creature.getTribe().equals(TribeClass.IDSEAL_PCGUARD)) {
			for (Npc npc : getOwner().getPosition().getWorldMapInstance().getNpcs(236248))
				getOwner().getAggroList().addHate(npc, 10000);
				
			for (Npc npc : getOwner().getPosition().getWorldMapInstance().getNpcs(236249))
				getOwner().getAggroList().addHate(npc, 10000);
		}
	}
}
