package ai.instance.drakenspire;

import ai.ActionItemNpcAI2;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
@AIName("beritra_portal")
public class BeritraPortalAI2 extends ActionItemNpcAI2 {

	@Override
	protected void handleUseItemFinish(final Player player) {
		switch (Rnd.get(1, 3)) {
			case 1:
				TeleportService2.teleportTo(player, 301390000, 174.7f, 518.2f, 1749.6f, (byte) 59, TeleportAnimation.FADE_OUT);
				break;
			case 2:

				break;
			case 3:

				break;
		}
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 915));
			}
		}, 3000);
	}
}
