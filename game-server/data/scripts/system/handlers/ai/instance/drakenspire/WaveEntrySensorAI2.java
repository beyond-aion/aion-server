package ai.instance.drakenspire;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;

import ai.AggressiveNpcAI2;

/**
 * @author Estrayl
 */
@AIName("wave_entry_sensor")
public class WaveEntrySensorAI2 extends AggressiveNpcAI2 {

	@Override
	public void handleCreatureDetected(Creature creature) {
		super.handleCreatureDetected(creature);
		if (creature instanceof Player)
			getOwner().getController().delete();
	}
}
