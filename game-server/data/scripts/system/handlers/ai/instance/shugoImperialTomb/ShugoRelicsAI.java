package ai.instance.shugoImperialTomb;

import java.util.Collection;
import java.util.HashSet;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.drop.DropService;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

import ai.ActionItemNpcAI;

/**
 * @author Ritsu
 */
@AIName("shugo_relic")
public class ShugoRelicsAI extends ActionItemNpcAI {

	@Override
	protected void handleUseItemFinish(Player player) {
		int npcId = getOwner().getNpcId();
		if ((npcId == 831122 || npcId == 831123 || npcId == 831124)
			&& (player.getInventory().decreaseByItemId(185000129, 1) || player.getInventory().decreaseByItemId(185000128, 1))) {
			analyzeOpening(player);
			return;
		} else if (npcId == 831373 && (player.getInventory().decreaseByItemId(185000129, 3) || player.getInventory().decreaseByItemId(185000128, 3))) {
			analyzeOpening(player);
			return;
		} else {
			if (npcId == 831373)
				PacketSendUtility.broadcastPacket(player, new SM_MESSAGE(player, "I could use 3 keys to open this box", ChatType.NORMAL), true);
			else
				PacketSendUtility.broadcastPacket(player, new SM_MESSAGE(player, "I could use 1 key to open this box", ChatType.NORMAL), true);
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401587));
			return;
		}
	}

	private void analyzeOpening(Player player) {
		if (getOwner().isInState(CreatureState.DEAD)) {
			AuditLogger.info(player, "Attempted multiple Chest looting!");
			return;
		}

		AIActions.die(this, player);
		Collection<Player> players = new HashSet<>();
		if (player.isInGroup()) {
			for (Player member : player.getPlayerGroup().getOnlineMembers()) {
				if (PositionUtil.isInRange(member, getOwner(), GroupConfig.GROUP_MAX_DISTANCE)) {
					players.add(member);
				}
			}
		} else if (player.isInAlliance()) {
			for (Player member : player.getPlayerAlliance().getOnlineMembers()) {
				if (PositionUtil.isInRange(member, getOwner(), GroupConfig.GROUP_MAX_DISTANCE)) {
					players.add(member);
				}
			}
		} else {
			players.add(player);
		}
		DropRegistrationService.getInstance().registerDrop(getOwner(), player, getHighestLevel(players), players);
		DropService.getInstance().requestDropList(player, getObjectId());
	}

	private int getHighestLevel(Collection<Player> players) {
		return players.stream().mapToInt(p -> p.getLevel()).max().getAsInt(); 
	}
}
