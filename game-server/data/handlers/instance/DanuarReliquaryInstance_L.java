package instance;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Cheatkiller
 * @modified Estrayl October 29th, 2017.
 */
@InstanceID(301330000)
public class DanuarReliquaryInstance_L extends DanuarReliquaryInstance {

	public DanuarReliquaryInstance_L(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected int getExitId() {
		return 730843;
	}

	@Override
	protected int getTreasureBoxId() {
		return 802183;
	}

}
