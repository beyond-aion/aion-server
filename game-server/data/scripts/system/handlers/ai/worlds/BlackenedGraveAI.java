package ai.worlds;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.GeneralNpcAI;

/**
 * @author Tibald
 */
@AIName("blackened_grave")
public class BlackenedGraveAI extends GeneralNpcAI {

	public BlackenedGraveAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		spawn(284262, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
		AIActions.deleteOwner(this);
	}
}
