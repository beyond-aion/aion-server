package ai.instance.drakenspire;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("wave_entry_sensor")
public class WaveEntrySensorAI extends AggressiveNpcAI {

	public WaveEntrySensorAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleCreatureDetected(Creature creature) {
		super.handleCreatureDetected(creature);
		if (creature instanceof Player)
			getOwner().getController().delete();
	}
}
