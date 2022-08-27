package instance;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(310050000)
public class AetherogeneticsLabInstance extends GeneralInstanceHandler {

	public AetherogeneticsLabInstance(WorldMapInstance instance) {
		super(instance);
	}
}
