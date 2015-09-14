package consolecommands;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.services.player.PlayerVisualStateService;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */

public class Invisible extends ConsoleCommand {

	public Invisible() {
		super("invisible");
	}

	@Override
	public void execute(Player admin, String... params) {

		if (admin.getVisualState() < 3) {
			admin.getEffectController().setAbnormal(AbnormalState.HIDE.getId());
			admin.setVisualState(CreatureVisualState.HIDE20);
			PacketSendUtility.broadcastPacket(admin, new SM_PLAYER_STATE(admin), true);
			PacketSendUtility.sendMessage(admin, "You are invisible.");
			if (SecurityConfig.INVIS) {
				PlayerVisualStateService.hideValidate(admin);
			}
		} else {
			PacketSendUtility.sendMessage(admin, "You are invisible.");
		}
	}
}
