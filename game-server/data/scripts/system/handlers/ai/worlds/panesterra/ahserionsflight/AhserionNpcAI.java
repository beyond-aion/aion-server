package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawnTemplate;

import ai.AggressiveNpcAI;

/**
 * @author Yeats
 */
@AIName("ahserion_aggressive_npc")
public class AhserionNpcAI extends AggressiveNpcAI {

	public AhserionNpcAI(Npc owner) {
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
	protected AhserionsFlightSpawnTemplate getSpawnTemplate() {
		return (AhserionsFlightSpawnTemplate) super.getSpawnTemplate();
	}
}
