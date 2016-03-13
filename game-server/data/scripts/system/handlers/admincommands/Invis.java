package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Divinity
 * @modified Neon
 */
public class Invis extends AdminCommand {

	public Invis() {
		super("invis", "Sets/unsets advanced invisibility.");
	}

	@Override
	public void execute(Player player, String... params) {
		if (!player.isInVisualState(CreatureVisualState.HIDE20)) {
			player.getEffectController().setAbnormal(AbnormalState.HIDE.getId());
			player.setVisualState(CreatureVisualState.HIDE20);
			player.getController().onHide();
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_EFFECT_INVISIBLE_BEGIN);
		} else {
			player.getController().onHideEnd(); // must go before updating visual state
			player.getEffectController().unsetAbnormal(AbnormalState.HIDE.getId());
			player.unsetVisualState(CreatureVisualState.HIDE20);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_EFFECT_INVISIBLE_END);
		}
		PacketSendUtility.broadcastPacket(player, new SM_PLAYER_STATE(player), true);
	}
}
