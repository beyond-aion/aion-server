package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AI2Request;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionInstance;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraMatchmakingService;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI2;

/**
 * @author Yeats
 *
 */
@AIName("ahserion_registration_corridor")
public class AhserionRegistrationCorridor extends GeneralNpcAI2 {
	
	@Override
	public void handleDialogStart(Player player) {
		if (player.getLevel() < 65) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402400)); //cant enter at your level
			return;
		}
		AI2Actions.addRequest(this, player, 905067, 0, new AI2Request() {

		@Override
		public void acceptRequest(Creature requester, Player responder, int requestId) {
			if (AhserionInstance.getInstance().isStarted()) {
				if (PanesterraMatchmakingService.getInstance().registerPlayer(player)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_REGISTER_SUCCESS);
					return;
				}
			}
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_NOTICE);
		}
		});
	}
}
