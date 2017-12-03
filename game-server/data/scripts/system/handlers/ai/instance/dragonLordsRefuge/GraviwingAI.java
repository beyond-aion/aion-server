package ai.instance.dragonLordsRefuge;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("graviwing")
public class GraviwingAI extends AggressiveNpcAI {

	public GraviwingAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		isDeadGod();
	}

	private boolean isDeadGod() {
		Npc marcutan = getNpc(219491);
		Npc kaisinel = getNpc(219488);
		if (isDead(marcutan) || isDead(kaisinel)) {
			AIActions.useSkill(this, 20983);
			return true;
		}
		return false;
	}

	private boolean isDead(Npc npc) {
		return (npc != null && npc.isDead());
	}

	private Npc getNpc(int npcId) {
		return getPosition().getWorldMapInstance().getNpc(npcId);
	}
}
