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

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		if (Rnd.get(1, 100) > 25) { // Pegureronerk
			spawn(798376, 516.198364f, 489.708008f, 885.760315f, (byte) 60);
		} else { // Kaneron Agent
			spawn(215041, 516.198f, 489.708f, 885.76f, (byte) 60);
		}
		int npcId = 0;
		switch (Rnd.get(1, 6)) { // Special Delivery
			case 1:
				npcId = 215074;
				break;
			case 2:
				npcId = 215075;
				break;
			case 3:
				npcId = 215076;
				break;
			case 4:
				npcId = 215077;
				break;
			case 5:
				npcId = 215054;
				break;
			case 6:
				npcId = 215055;
				break;
		}
		spawn(npcId, 461.933350f, 510.545654f, 877.618103f, (byte) 90);
	}
}
