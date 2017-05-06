package ai.instance.beshmundirTemple;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIRequest;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FIND_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.PortalService;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author Gigi, vlog
 */
@AIName("beshmundirswalk")
public class BeshmundirsWalkAI extends ActionItemNpcAI {

	@Override
	protected void handleUseItemFinish(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10)); // Initial dialog
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		AIRequest request = new AIRequest() {

			@Override
			public void acceptRequest(Creature requester, Player responder, int requestId) {
				// TODO: create an instance, depending on difficulty level
				if (requestId == SM_QUESTION_WINDOW.STR_INSTANCE_DUNGEON_WITH_DIFFICULTY_ENTER_CONFIRM) {
					moveToInstance(responder, (byte) 2);
				} else {
					moveToInstance(responder, (byte) 1);
				}
			}

		};
		switch (dialogActionId) {
			case OPEN_INSTANCE_RECRUIT:
				AutoGroupType agt = AutoGroupType.getAutoGroup(player.getLevel(), getNpcId());
				if (agt != null) {
					PacketSendUtility.sendPacket(player, new SM_FIND_GROUP(0x1A, agt.getInstanceMapId()));
				}
				break;
			case INSTANCE_ENTRY: // I'm ready to enter
				if (!player.isInGroup()) {
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1390256));
					return true;
				}
				if (player.getPlayerGroup().isLeader(player)) {
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 4762)); // Path selection
				} else {
					if (!isAGroupMemberInInstance(player)) {
						PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400361));
						return true;
					}
					moveToInstance(player, (byte) 0);
				}
				break;
			case SELECT_NONE_1: // I'll take the safer path
				AIActions.addRequest(this, player, SM_QUESTION_WINDOW.STR_INSTANCE_DUNGEON_WITH_DIFFICULTY_ENTER_CONFIRM, getObjectId(), request, "300170000",
					new DescriptionId(1804103));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 4762)); // Path selection
				break;
			case SELECT_NONE_2: // Give me the dangerous path
				AIActions.addRequest(this, player, SM_QUESTION_WINDOW.STR_INSTANCE_DUNGEON_WITH_DIFFICULTY_ENTER_CONFIRM, getObjectId(), request, "300170000",
					new DescriptionId(1804105));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 4762)); // Path selection
				break;
		}
		return true;
	}

	private boolean isAGroupMemberInInstance(Player player) {
		if (player.isInGroup()) {
			for (Player member : player.getPlayerGroup().getMembers()) {
				if (member.getWorldId() == 300170000) {
					return true;
				}
			}
		} else {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ENTER_ONLY_PARTY_DON());
		}
		return false;
	}

	private void moveToInstance(Player player, byte difficult) {
		PortalPath portalPath = DataManager.PORTAL2_DATA.getPortalUsePath(getNpcId(), player);
		if (portalPath != null) {
			PortalService.port(portalPath, player, getOwner(), difficult);
		}
	}

}
