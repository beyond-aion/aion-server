package ai.instance.dragonLordsRefuge;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;


/**
 * @author Cheatkiller
 *
 */
@AIName("graviwing")
public class GraviwingAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleAttack(Creature creature){
		super.handleAttack(creature);
		isDeadGod();
	}
	
	private boolean isDeadGod() {
		Npc marcutan = getNpc(219491);
		Npc kaisinel = getNpc(219488);
		if (isDead(marcutan) || isDead(kaisinel)) {
			AI2Actions.useSkill(this, 20983);
			return true;
		}
		return false;
	}
	
	private boolean isDead(Npc npc) {
		return (npc != null && npc.getLifeStats().isAlreadyDead());
	}
	
	private Npc getNpc(int npcId) {
		return getPosition().getWorldMapInstance().getNpc(npcId);
	}
}
