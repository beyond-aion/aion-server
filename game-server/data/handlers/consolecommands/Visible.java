package consolecommands;

import java.util.Collections;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABNORMAL_STATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1, Neon
 */
public class Visible extends ConsoleCommand {

	public Visible() {
		super("visible", "Unsets advanced invisibility.");
	}

	@Override
	public void execute(Player player, String... params) {
		if (player.isInVisualState(CreatureVisualState.HIDE20)) {
			player.getEffectController().unsetAbnormal(AbnormalState.HIDE);
			player.unsetVisualState(CreatureVisualState.HIDE20);
			player.getController().onHideEnd();
			PacketSendUtility.broadcastPacket(player, new SM_PLAYER_STATE(player), true);
		}
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_EFFECT_INVISIBLE_END());
		// required because without a skill this isn't sent automatically (outdated abnormals can cause issues when opening a private store for example)
		PacketSendUtility.sendPacket(player, new SM_ABNORMAL_STATE(Collections.emptyList(), player.getEffectController().getAbnormals(), 0));
	}
}
