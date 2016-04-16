package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
@AIName("empadministratorarminos")
public class EmpyreanAdministratorArminosAI2 extends NpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startEvent();
	}

	private void startEvent() {
		switch (getNpcId()) {
			case 217744:
				PacketSendUtility.broadcastMessage(getOwner(), 1500247, 8000);
				PacketSendUtility.broadcastMessage(getOwner(), 1500250, 20000);
				PacketSendUtility.broadcastMessage(getOwner(), 1500251, 60000);
				break;
			case 217749:
				PacketSendUtility.broadcastMessage(getOwner(), 1500252, 8000);
				PacketSendUtility.broadcastMessage(getOwner(), 1500253, 16000);
				PacketSendUtility.broadcastToMap(getOwner(), 1400982, 25000);
				PacketSendUtility.broadcastToMap(getOwner(), 1400988, 27000);
				PacketSendUtility.broadcastToMap(getOwner(), 1400989, 29000);
				PacketSendUtility.broadcastToMap(getOwner(), 1400990, 31000);
				PacketSendUtility.broadcastToMap(getOwner(), 1401013, 93000);
				PacketSendUtility.broadcastToMap(getOwner(), 1401014, 113000);
				PacketSendUtility.broadcastToMap(getOwner(), 1401015, 118000);
				PacketSendUtility.broadcastMessage(getOwner(), 1500255, 118000);
				break;
		// case
		// despawn after 1min
		}
	}
}
