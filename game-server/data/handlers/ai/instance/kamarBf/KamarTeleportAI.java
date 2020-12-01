package ai.instance.kamarBf;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.portals.PortalAI;

/**
 * @author Luzien
 */
@AIName("kamar_teleport")
public class KamarTeleportAI extends PortalAI {

	protected int remainingUses;

	public KamarTeleportAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		remainingUses = 25;
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (remainingUses <= 0) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1500906));
			return;
		}
		super.handleUseItemFinish(player);
		if (--remainingUses == 0)
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1500906));
		else
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1500905, remainingUses));
	}

}
