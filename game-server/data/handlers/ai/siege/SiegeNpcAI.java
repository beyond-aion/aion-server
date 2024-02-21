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
		return switch (question) {
			case DECAY, RESPAWN, LOOT, REMOVE_EFFECTS_ON_MAPREGION_DEACTIVATE -> false;
			default -> super.ask(question);
		};
	}

	@Override
	protected SiegeSpawnTemplate getSpawnTemplate() {
		return (SiegeSpawnTemplate) super.getSpawnTemplate();
	}
}
