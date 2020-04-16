package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Cheatkiller, Sykra
 */
@AIName("flag")
public class FlagNpcAI extends NpcAI {

	public FlagNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	public int modifyDamage(Creature attacker, int damage, Effect effect) {
		return 0;
	}

	@Override
	public int modifyOwnerDamage(int damage, Creature effected, Effect effect) {
		return 0;
	}

}
