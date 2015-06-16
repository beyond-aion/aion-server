package consolecommands;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.services.player.PlayerVisualStateService;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ginho1
 */

public class Visible extends ConsoleCommand {

	public Visible() {
		super("visible");
	}

	@Override
	public void execute(Player admin, String... params) {

		if (admin.getVisualState() >= 3) {
			admin.getEffectController().unsetAbnormal(AbnormalState.HIDE.getId());
			admin.unsetVisualState(CreatureVisualState.HIDE20);
			PacketSendUtility.broadcastPacket(admin, new SM_PLAYER_STATE(admin), true);
			PacketSendUtility.sendMessage(admin, "You are visible.");
			if (SecurityConfig.INVIS) {
				PlayerVisualStateService.hideValidate(admin);
			}
		}
		else {
			PacketSendUtility.sendMessage(admin, "You are visible.");
		}
	}
}