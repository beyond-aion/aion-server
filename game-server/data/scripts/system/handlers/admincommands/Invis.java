package admincommands;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.services.player.PlayerVisualStateService;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Divinity
 */
public class Invis extends AdminCommand {

	public Invis() {
		super("invis");
	}

	@Override
	public void execute(Player player, String... params) {
		if (player.getVisualState() < 3) {
			player.getEffectController().setAbnormal(AbnormalState.HIDE.getId());
			player.setVisualState(CreatureVisualState.HIDE20);
			PacketSendUtility.broadcastPacket(player, new SM_PLAYER_STATE(player), true);
			PacketSendUtility.sendMessage(player, "You are invisible.");
			if (SecurityConfig.INVIS) {
				PlayerVisualStateService.hideValidate(player);
			}
		}
		else {
			player.getEffectController().unsetAbnormal(AbnormalState.HIDE.getId());
			player.unsetVisualState(CreatureVisualState.HIDE20);
			PacketSendUtility.broadcastPacket(player, new SM_PLAYER_STATE(player), true);
			PacketSendUtility.sendMessage(player, "You are visible.");
			if (SecurityConfig.INVIS) {
				PlayerVisualStateService.hideValidate(player);
			}
		}
	}

	@Override
	public void onFail(Player player, String message) {
		// TODO Auto-generated method stub
	}

}