package ai.quests;

import com.aionemu.gameserver.ai.AIName;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("ascensationquestnpc")
public class AscensationNpcAI extends AggressiveNpcAI {

	@Override
	public int modifyOwnerDamage(int damage) {
		return 1;
	}

}
