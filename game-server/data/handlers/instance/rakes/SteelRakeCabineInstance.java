package instance.rakes;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300460000)
public class SteelRakeCabineInstance extends GeneralInstanceHandler {

	public SteelRakeCabineInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		if (Rnd.get(1, 2) == 1) { // Sweeper Nunukin
			spawn(219026, 353.814f, 491.557f, 949.466f, (byte) 119);
		} else {
			spawn(219026, 354.7875f, 536.17004f, 949.4662f, (byte) 7);
		}
		int chance = Rnd.get(1, 2);
		// Madame Bovariki + Steel Rake Shaman
		spawn(chance == 1 ? 219032 : 219003, 463.124f, 512.75f, 952.545f, (byte) 0);
		spawn(chance == 1 ? 219003 : 219032, 502.859f, 548.55f, 952.417f, (byte) 85);
	}
}
