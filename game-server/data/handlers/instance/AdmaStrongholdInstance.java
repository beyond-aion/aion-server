package instance;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Tibald
 */
@InstanceID(320130000)
public class AdmaStrongholdInstance extends GeneralInstanceHandler {

	public AdmaStrongholdInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		if (Rnd.nextBoolean()) {
			switch (Rnd.get(1, 3)) {
				case 1 -> spawn(205224, 477.5849f, 398.20898f, 187.49918f, (byte) 50);
				case 2 -> spawn(205224, 347.76538f, 559.4453f, 180.33097f, (byte) 105);
				case 3 -> spawn(205224, 528.25116f, 555.0943f, 189.49f, (byte) 78);
			}
		}
	}
}
