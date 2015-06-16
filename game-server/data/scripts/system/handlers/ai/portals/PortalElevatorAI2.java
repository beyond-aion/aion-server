package ai.portals;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
@AIName("portal_elevator")
public class PortalElevatorAI2 extends PortalAI2 {

	@Override
	protected void handleUseItemFinish(Player player) {
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(getOwner(), EmotionType.EMOTE, 144, 0), true);
		super.handleUseItemFinish(player);
	}

}