package ai.siege;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Bobobear
 */
@AIName("base_flag")
public class FlagBaseNpcAI extends NpcAI {

	public FlagBaseNpcAI(Npc owner) {
		super(owner);
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
