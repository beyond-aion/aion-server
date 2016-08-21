package ai.quests;

import com.aionemu.gameserver.ai2.AIName;

import ai.AggressiveNpcAI2;

/**
 * @author Cheatkiller
 */
@AIName("ascensationquestnpc")
public class AscensationNpcAI2 extends AggressiveNpcAI2 {

	@Override
	public int modifyOwnerDamage(int damage) {
		return 1;
	}

}
