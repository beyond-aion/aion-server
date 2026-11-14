package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
@AIName("empadministratorarminos")
public class EmpyreanAdministratorArminosAI extends NpcAI {

	public EmpyreanAdministratorArminosAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startEvent();
	}

	private void startEvent() {
		switch (getNpcId()) {
			case 217744 -> {
				PacketSendUtility.broadcastMessage(getOwner(), 1500247, 8000);
				PacketSendUtility.broadcastMessage(getOwner(), 1500250, 20000);
				PacketSendUtility.broadcastMessage(getOwner(), 1500251, 92000);
			}
			case 217749 -> {
				PacketSendUtility.broadcastMessage(getOwner(), 1500252, 8000);
				PacketSendUtility.broadcastMessage(getOwner(), 1500253, 16000);
				PacketSendUtility.broadcastToMap(getOwner(), 1400982, 24000);
				PacketSendUtility.broadcastToMap(getOwner(), 1400988, 26000);
				PacketSendUtility.broadcastToMap(getOwner(), 1400989, 28000);
				PacketSendUtility.broadcastToMap(getOwner(), 1400990, 30000);
			}
		}
	}
}
