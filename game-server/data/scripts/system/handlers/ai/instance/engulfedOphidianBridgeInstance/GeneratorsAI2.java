package ai.instance.engulfedOphidianBridgeInstance;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI2;

/**
 * @author Cheatkiller
 */
@AIName("engulfedophidiangens")
public class GeneratorsAI2 extends ActionItemNpcAI2 {

	private Npc flag;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		switch (getOwner().getSpawn().getStaticId()) {
			case 160:
				flag = (Npc) spawn(802038, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0); // NORTH
				PacketSendUtility.broadcastToMap(getOwner(), 1402001);
				break;
			case 164:
				flag = (Npc) spawn(802035, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0); // GUARD
				PacketSendUtility.broadcastToMap(getOwner(), 1402000);
				break;
			case 158:
				flag = (Npc) spawn(802041, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0); // SOUTHERN
				PacketSendUtility.broadcastToMap(getOwner(), 1402002);
				break;
			case 162:
				flag = (Npc) spawn(802044, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0); // Defence
				PacketSendUtility.broadcastToMap(getOwner(), 1402003);
				break;
			case 175:
				flag = (Npc) spawn(802036, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0); // NORTH
				PacketSendUtility.broadcastToMap(getOwner(), 1401962);
				break;
			case 172:
				flag = (Npc) spawn(802033, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0); // GUARD
				PacketSendUtility.broadcastToMap(getOwner(), 1401961);
				break;
			case 169:
				flag = (Npc) spawn(802039, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0); // SOUTHERN
				PacketSendUtility.broadcastToMap(getOwner(), 1401963);
				break;
			case 171:
				flag = (Npc) spawn(802042, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0); // Defence
				PacketSendUtility.broadcastToMap(getOwner(), 1401964);
				break;
			case 174:
				flag = (Npc) spawn(802037, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0); // NORTH
				PacketSendUtility.broadcastToMap(getOwner(), 1401992);
				break;
			case 173:
				flag = (Npc) spawn(802034, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0); // GUARD
				PacketSendUtility.broadcastToMap(getOwner(), 1401991);
				break;
			case 166:
				flag = (Npc) spawn(802040, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0); // SOUTHERN
				PacketSendUtility.broadcastToMap(getOwner(), 1401993);
				break;
			case 170:
				flag = (Npc) spawn(802043, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0); // Defence
				PacketSendUtility.broadcastToMap(getOwner(), 1401994);
				break;

		}
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
