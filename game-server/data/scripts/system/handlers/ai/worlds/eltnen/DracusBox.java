package ai.worlds.eltnen;

import ai.OneDmgPerHitAI2;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Neon
 */
@AIName("dracusbox")
public class DracusBox extends OneDmgPerHitAI2 {

	@Override
	protected void handleDied() {
		super.handleDied();

		WorldPosition pos = getOwner().getPosition();

		switch (Rnd.get(1, 3)) {
			case 1:
				spawn(211792, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading()); // elroco
				break;
			case 2:
				spawn(211799, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading()); // oozing clodworm
				break;
			case 3:
				spawn(211800, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading()); // chaos dracus
				break;
		}
		getOwner().getController().onDespawn();
	}
}
