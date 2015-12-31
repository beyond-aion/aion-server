package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawnTemplate;

import ai.AggressiveNpcAI2;

/**
 * @author Yeats
 *
 */
@AIName("ahserion_aggressive_npc")
public class AhserionNpcAI2 extends AggressiveNpcAI2 {
	
	@Override
	protected AIAnswer pollInstance(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
				return AIAnswers.NEGATIVE;
			case SHOULD_RESPAWN:
				return AIAnswers.NEGATIVE;
			case SHOULD_REWARD:
				return AIAnswers.POSITIVE;
			case SHOULD_LOOT:
				return AIAnswers.NEGATIVE;
			default:
				return null;
		}
	}
	
	@Override
	protected AhserionsFlightSpawnTemplate getSpawnTemplate() {
		return (AhserionsFlightSpawnTemplate) super.getSpawnTemplate();
	}
}
