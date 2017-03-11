package ai.quests;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.skillengine.model.Effect;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("ascensationquestnpc")
public class AscensationNpcAI extends AggressiveNpcAI {

	@Override
	public int modifyOwnerDamage(int damage, Effect effect) {
		return 1;
	}

}
