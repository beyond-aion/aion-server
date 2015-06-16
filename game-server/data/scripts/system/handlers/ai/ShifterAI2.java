package ai;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 * 
 */
@AIName("shifter")
public class ShifterAI2 extends ActionItemNpcAI2 {

	@Override
	protected void handleUseItemFinish(Player player) {
		super.handleUseItemFinish(player);
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(getOwner(), EmotionType.EMOTE, 144, 0), true);
	}
}