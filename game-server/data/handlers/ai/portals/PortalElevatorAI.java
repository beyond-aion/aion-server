package ai.portals;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
@AIName("portal_elevator")
public class PortalElevatorAI extends PortalAI {

	public PortalElevatorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(getOwner(), EmotionType.EMOTE, 144, 0), true);
		super.handleUseItemFinish(player);
	}

}
