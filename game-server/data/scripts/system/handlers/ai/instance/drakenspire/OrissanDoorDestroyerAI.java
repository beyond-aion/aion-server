package ai.instance.drakenspire;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("orissan_door_destroyer")
public class OrissanDoorDestroyerAI extends GeneralNpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleGateDestruction();
	}

	private void scheduleGateDestruction() {
		PacketSendUtility.broadcastMessage(getOwner(), 1501313, 7000);
		ThreadPoolManager.getInstance().schedule(() -> {
			for (Npc npc : getOwner().getPosition().getWorldMapInstance().getNpcs())
				if (npc.getNpcId() == 731580 && isInRange(npc, 15))
					SkillEngine.getInstance().getSkill(npc, 20840, 1, npc).useWithoutPropSkill();
		}, 14000);
	}
}
