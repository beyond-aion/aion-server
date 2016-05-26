package ai.instance.drakenspire;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
@AIName("wave_defender")
public class WaveDefenderAI2 extends AggressiveNpcAI2 {

	private AtomicBoolean isDestinationReached = new AtomicBoolean();

	@Override
	public void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getMoveController().isStop() && isDestinationReached.compareAndSet(false, true))
				scheduleLocationUpdate();
	}
	
	private void scheduleLocationUpdate() {
		ThreadPoolManager.getInstance().schedule(() -> {
				spawn(getOwner().getRace() == Race.ELYOS ? 236248 : 236249, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getHeading());
				getOwner().getController().onDelete();
		}, 3000);
	}
	
	private byte getHeading() {
		byte h = 30;
		switch (getOwner().getSpawn().getWalkerId()) {
			case "301390000_NPCPathFunction_Npc_Path07":
				h = 100;
				break;
			case "301390000_NPCPathFunction_Npc_Path08":
				h = 110;
				break;
			case "301390000_NPCPathFunction_Npc_Path09":
				h = 3;
				break;
			case "301390000_NPCPathFunction_Npc_Path10":
				h = 10;
				break;
			case "301390000_NPCPathFunction_Npc_Path13":
				h = 77;
				break;
			case "301390000_NPCPathFunction_Npc_Path14":
				h = 64;
				break;
			case "301390000_NPCPathFunction_Npc_Path15":
				h = 64;
				break;
			case "301390000_NPCPathFunction_Npc_Path16":
				h = 55;
				break;
		}
		return h;
	}
}
