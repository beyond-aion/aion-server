package ai.instance.drakenspire;

import ai.GeneralNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Estrayl
 */
@AIName("orissan_door_destroyer")
public class OrissanDoorDestroyerAI2 extends GeneralNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleGateDestruction();
	}

	private void scheduleGateDestruction() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(getOwner(), new SM_SYSTEM_MESSAGE(true, 1501313, getOwner().getObjectId(), 1));
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						for (Npc npc : getOwner().getPosition().getWorldMapInstance().getNpcs()) {
							if (npc.getNpcId() == 731580 && isInRange(npc, 15))
								SkillEngine.getInstance().getSkill(npc, 20840, 1, npc).useWithoutPropSkill();
						}
					}
				}, 7000);
			}
		}, 7000);
	}
}
