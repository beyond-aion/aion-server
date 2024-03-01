package ai.instance.theShugoEmperorsVault;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.skillengine.model.Effect;

import ai.AggressiveNoLootNpcAI;

/**
 * @author Yeats
 */
@AIName("IDSweep_shugos")
public class IDSweep_Shugos extends AggressiveNoLootNpcAI {

	private int baseDamage;

	public IDSweep_Shugos(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		InstanceHandler handler = getOwner().getPosition().getWorldMapInstance().getInstanceHandler();
		InstanceScore<?> reward;
		if (handler != null) {
			reward = handler.getInstanceScore();
			if (reward != null) {
				if (reward.getInstanceProgressionType() == InstanceProgressionType.END_PROGRESS)
					getOwner().getController().delete();
			}
		}
		baseDamage = getOwner().getGameStats().getStatsTemplate().getAttack();
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		if (effect == null) {
			int rndDamage = Rnd.get(-Math.round(baseDamage * 0.2f), Math.round(baseDamage * 0.25f));
			damage = baseDamage + rndDamage;
		}
		return damage;
	}
}
