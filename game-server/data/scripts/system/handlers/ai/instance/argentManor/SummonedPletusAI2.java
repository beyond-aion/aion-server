package ai.instance.argentManor;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;

/**
 *
 * @author xTz
 */
@AIName("summoned_pletus")
public class SummonedPletusAI2 extends AggressiveNpcAI2 {

	@Override
	public boolean canThink() {
		return false;
	}

}