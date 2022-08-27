package instance;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(320120000)
public class ShadowCourtInstance extends GeneralInstanceHandler {

	public ShadowCourtInstance(WorldMapInstance instance) {
		super(instance);
	}
}
