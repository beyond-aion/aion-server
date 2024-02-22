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
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_LOOT -> false;
			default -> super.ask(question);
		};
	}

	@Override
	protected AhserionsFlightSpawnTemplate getSpawnTemplate() {
		return (AhserionsFlightSpawnTemplate) super.getSpawnTemplate();
	}
}
