package instance.rakes;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300100000)
public class SteelRakeInstance extends GeneralInstanceHandler {

	public SteelRakeInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		if (Rnd.chance() < 75) { // Pegureronerk
			spawn(798376, 516.198364f, 489.708008f, 885.760315f, (byte) 60);
		} else { // Kaneron Agent
			spawn(215041, 516.198f, 489.708f, 885.76f, (byte) 60);
		}
		int specialDelivery = Rnd.get(new int[] {215074, 215075, 215076, 215077, 215054, 215055});
		spawn(specialDelivery, 461.933350f, 510.545654f, 877.618103f, (byte) 90);
	}
}
