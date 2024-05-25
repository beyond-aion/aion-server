package ai.instance.drakenspire;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("wave_defender")
public class WaveDefenderAI extends AggressiveNpcAI {

	private AtomicBoolean isDestinationReached = new AtomicBoolean();

	public WaveDefenderAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getMoveController().isStop() && isDestinationReached.compareAndSet(false, true))
			scheduleLocationUpdate();
	}

	private void scheduleLocationUpdate() {
		ThreadPoolManager.getInstance().schedule(() -> {
			spawn(getOwner().getRace() == Race.ELYOS ? 236248 : 236249, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getHeading());
			getOwner().getController().delete();
		}, 3000);
	}

	private byte getHeading() {
		return switch (getOwner().getSpawn().getWalkerId()) {
			case "301390000_NPCPathFunction_Npc_Path07" -> 100;
			case "301390000_NPCPathFunction_Npc_Path08" -> 110;
			case "301390000_NPCPathFunction_Npc_Path09" -> 3;
			case "301390000_NPCPathFunction_Npc_Path10" -> 10;
			case "301390000_NPCPathFunction_Npc_Path13" -> 77;
			case "301390000_NPCPathFunction_Npc_Path14" -> 64;
			case "301390000_NPCPathFunction_Npc_Path15" -> 64;
			case "301390000_NPCPathFunction_Npc_Path16" -> 55;
			default -> 30;
		};
	}
}
