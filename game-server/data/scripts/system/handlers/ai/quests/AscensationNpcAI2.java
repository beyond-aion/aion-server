package ai.quests;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;


/**
 * @author Cheatkiller
 *
 */
@AIName("ascensationquestnpc")
public class AscensationNpcAI2 extends AggressiveNpcAI2 {

	@Override
	public int modifyOwnerDamage(int damage) {
		return 1;
	}

}