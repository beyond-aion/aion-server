package ai.instance.kamarBf;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.ChestAI2;

/**
 * @author Cheatkiller
 */
@AIName("reiansupplyitems")
public class ReianSupplyItemsAI2 extends ChestAI2 {

	private Npc flag;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		flag = (Npc) spawn(801959, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		if (flag != null)
			flag.getController().onDelete();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		if (flag != null)
			flag.getController().onDelete();
	}
}
