package instance.abyss;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * Created on June 23rd, 2016
 *
 * @author Estrayl
 * @since AION 4.8
 */
@InstanceID(301260000)
public class LegionsKrotanBarracks extends KrotanBarracks {

	public LegionsKrotanBarracks(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected int getChestId() {
		return 702290;
	}
}
