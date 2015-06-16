package instance.pvparenas;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300430000)
public class DisciplineTrainingGroundsInstance extends PvPArenaInstance {

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		killBonus = 200;
		deathFine = -100;
		super.onInstanceCreate(instance);
	}

}