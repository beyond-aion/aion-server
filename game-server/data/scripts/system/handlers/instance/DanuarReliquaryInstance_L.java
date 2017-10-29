package instance;

import com.aionemu.gameserver.instance.handlers.InstanceID;

/**
 * @author Cheatkiller
 * @modified Estrayl October 29th, 2017.
 */
@InstanceID(301330000)
public class DanuarReliquaryInstance_L extends DanuarReliquaryInstance {

	@Override
	protected int getExitId() {
		return 730843;
	}

	@Override
	protected int getTreasureBoxId() {
		return 802183;
	}

}
