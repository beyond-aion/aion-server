package instance.abyss;

import com.aionemu.gameserver.instance.handlers.InstanceID;

/**
 * Created on June 23rd, 2016
 *
 * @author Estrayl
 * @since AION 4.8
 */
@InstanceID(301240000)
public class LegionsKysisBarracks extends KysisBarracks {

	@Override
	protected int getChestId() {
		return 702294;
	}
}
