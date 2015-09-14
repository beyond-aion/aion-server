package ai.worlds.idiandepth;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;

/**
 * @author Tibald
 */
@AIName("blackenedgrave")
public class BlackenedGraveAI2 extends GeneralNpcAI2 {

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		spawn(284262, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
		AI2Actions.deleteOwner(this);
	}
}
