package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai2.AIName;
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
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_LOOT:
				return false;
			default:
				return super.ask(question);
		}
	}
	
	@Override
	protected AhserionsFlightSpawnTemplate getSpawnTemplate() {
		return (AhserionsFlightSpawnTemplate) super.getSpawnTemplate();
	}
}
