package ai.instance.shugoImperialTomb;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("lediar_assistant")
public class LediarAssistantAI extends AggressiveNpcAI {

	private final static int[] npc_ids = { 831251, 831250, 831305 };

	public LediarAssistantAI(Npc owner) {
		super(owner);
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		return 1;
	}

	@Override
	protected void handleSpawned() {
		addHate();
		super.handleSpawned();
	}

	private void addHate() {
		EmoteManager.emoteStopAttacking(getOwner());
		for (int npc_id : npc_ids) {
			Npc tower = getOwner().getPosition().getWorldMapInstance().getNpc(npc_id);
			if (tower != null && !tower.isDead()) {
				switch (npc_id) {
					case 831305:
					case 831250:
					case 831251:
						getOwner().getAggroList().addHate(tower, 10000);
						break;
				}
			}
		}
	}
}
