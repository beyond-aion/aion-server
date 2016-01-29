package ai.instance.drakenspire;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
@AIName("twin_door_destroyer")
public class TwinDoorDestroyerAI2 extends GeneralNpcAI2 {

	private AtomicBoolean isGateReached = new AtomicBoolean(false);

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		removeTrap();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(getOwner(), new SM_SYSTEM_MESSAGE(true, 1501309, getOwner().getObjectId(), 1));
			}
		}, 2500);
	}

	@Override
	public void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getMoveController().isStop()) {
			if (isGateReached.compareAndSet(false, true)) {
				PacketSendUtility.broadcastPacket(getOwner(), new SM_SYSTEM_MESSAGE(true, 1501310, getOwner().getObjectId(), 1));
				scheduleGateAttack();
			}
		}
	}

	private void removeTrap() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				for (Npc npc : getOwner().getPosition().getWorldMapInstance().getNpcs()) {
					if (npc.getNpcId() == 207128 || npc.getNpcId() == 207129)
						npc.getController().onDelete();
				}
			}
		}, 1500);
	}

	private void scheduleGateAttack() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				for (Npc npc : getOwner().getPosition().getWorldMapInstance().getNpcs()) {
					if (npc.getNpcId() == 731580 && isInRange(npc, 10))
						SkillEngine.getInstance().getSkill(npc, 20840, 1, npc).useWithoutPropSkill();
				}
				PacketSendUtility.broadcastPacket(getOwner(), new SM_SYSTEM_MESSAGE(true, 1501311, getOwner().getObjectId(), 1));
			}
		}, 3500);
	}
}
