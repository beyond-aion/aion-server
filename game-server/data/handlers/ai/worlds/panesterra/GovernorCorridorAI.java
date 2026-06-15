package ai.worlds.panesterra;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author Estrayl
 */
@AIName("governor_advance_corridor")
public class GovernorCorridorAI extends AdvanceCorridorAI {

	public GovernorCorridorAI(Npc owner) {
		super(owner);
		despawnInMin = 5;
	}

	@Override
	public void handleDialogStart(Player player) {
		if (player.getAbyssRank().getRank() != AbyssRankEnum.SUPREME_COMMANDER) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_TELEPOTER_GAB1_USER05());
			return;
		}
		super.handleDialogStart(player);
	}
}
