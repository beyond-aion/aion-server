package ai.siege;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;

/**
 * @author ATracer
 */
public class SiegeNpcAI2 extends AggressiveNpcAI2 {

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
	protected SiegeSpawnTemplate getSpawnTemplate() {
		return (SiegeSpawnTemplate) super.getSpawnTemplate();
	}
}
