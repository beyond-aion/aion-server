package ai.instance.drakenspire;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("twin_door_destroyer")
public class TwinDoorDestroyerAI extends GeneralNpcAI {

	private AtomicBoolean isGateReached = new AtomicBoolean();

	public TwinDoorDestroyerAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		removeTrap();
		PacketSendUtility.broadcastMessage(getOwner(), 1501309, 2500);
	}

	@Override
	public void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getMoveController().isStop()) {
			if (isGateReached.compareAndSet(false, true)) {
				PacketSendUtility.broadcastMessage(getOwner(), 1501310);
				scheduleGateAttack();
			}
		}
	}

	private void removeTrap() {
		ThreadPoolManager.getInstance().schedule(() -> {
			for (Npc npc : getOwner().getPosition().getWorldMapInstance().getNpcs(207128, 207129))
				npc.getController().delete();
		}, 1500);
	}

	private void scheduleGateAttack() {
		ThreadPoolManager.getInstance().schedule(() -> {
			for (Npc npc : getOwner().getPosition().getWorldMapInstance().getNpcs(731580)) {
				if (isInRange(npc, 10))
					SkillEngine.getInstance().getSkill(npc, 20840, 1, npc).useWithoutPropSkill();
			}
			PacketSendUtility.broadcastMessage(getOwner(), 1501311);
		}, 3500);
	}
}
