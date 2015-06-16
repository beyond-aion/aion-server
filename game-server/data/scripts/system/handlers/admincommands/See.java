package admincommands;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureSeeState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.services.player.PlayerVisualStateService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Mathew
 */
public class See extends AdminCommand {

	public See() {
		super("see");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (admin.getSeeState() < 2) {
			admin.setSeeState(CreatureSeeState.SEARCH10);
			PacketSendUtility.broadcastPacket(admin, new SM_PLAYER_STATE(admin), true);
			PacketSendUtility.sendMessage(admin, "You got vision.");
			if (SecurityConfig.INVIS) {
				PlayerVisualStateService.seeValidate(admin);
			}
		}
		else {
			admin.unsetSeeState(CreatureSeeState.SEARCH10);
			PacketSendUtility.broadcastPacket(admin, new SM_PLAYER_STATE(admin), true);
			PacketSendUtility.sendMessage(admin, "You lost vision.");
			if (SecurityConfig.INVIS) {
				PlayerVisualStateService.seeValidate(admin);
			}
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //see");
	}

}