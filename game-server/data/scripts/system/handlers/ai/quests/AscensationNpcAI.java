package ai.quests;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("ascensationquestnpc")
public class AscensationNpcAI extends AggressiveNpcAI {

	public AscensationNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	public int modifyOwnerDamage(int damage, Effect effect) {
		return 1;
	}

}
