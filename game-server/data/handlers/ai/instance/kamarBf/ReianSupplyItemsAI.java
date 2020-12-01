package ai.instance.kamarBf;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.ChestAI;

/**
 * @author Cheatkiller
 */
@AIName("reiansupplyitems")
public class ReianSupplyItemsAI extends ChestAI {

	private Npc flag;

	public ReianSupplyItemsAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		flag = (Npc) spawn(801959, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		if (flag != null)
			flag.getController().delete();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		if (flag != null)
			flag.getController().delete();
	}
}
