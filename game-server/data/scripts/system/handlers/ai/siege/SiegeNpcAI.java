package ai.siege;

import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;

import ai.AggressiveNpcAI;

/**
 * @author ATracer
 */
public class SiegeNpcAI extends AggressiveNpcAI {

	public SiegeNpcAI(Npc owner) {
		super(owner);
	}

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
