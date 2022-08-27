package instance;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(320090000)
public class TrinielArenaInstance extends GeneralInstanceHandler {
	public TrinielArenaInstance(WorldMapInstance instance) {
		super(instance);
	}
}
