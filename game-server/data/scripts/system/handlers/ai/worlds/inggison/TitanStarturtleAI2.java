package ai.worlds.inggison;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.AggressiveNpcAI2;

/**
 * @author Cheatkiller
 */
@AIName("titanstarturtle")
public class TitanStarturtleAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleDied() {
		super.handleDied();
		spawn(getOwner().getWorldId(), 700545, 338.149f, 573.55f, 460, (byte) 0, 0, 1);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_SYSTEM_MESSAGE(1400486));
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		Npc windStream = getPosition().getWorldMapInstance().getNpc(700545);
		if (windStream != null)
			windStream.getController().delete();
	}
}
