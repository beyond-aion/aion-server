package ai.instance.beshmundirTemple;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIRequest;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.findgroup.FindGroupService;
import com.aionemu.gameserver.services.teleport.PortalService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author Gigi, vlog
 */
@AIName("beshmundirswalk")
public class BeshmundirsWalkAI extends ActionItemNpcAI {

	public BeshmundirsWalkAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10)); // Initial dialog
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		switch (dialogActionId) {
			case OPEN_INSTANCE_RECRUIT:
				FindGroupService.getInstance().showInstanceGroups(player, getOwner());
				break;
			case INSTANCE_ENTRY: // I'm ready to enter
				if (!player.isInGroup()) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ENTER_ONLY_PARTY_DON());
					return true;
				}
				if (player.getPlayerGroup().isLeader(player)) {
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 4762)); // Path selection
				} else {
					if (!isAGroupMemberInInstance(player)) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_DUNGEON_CANT_ENTER_NOT_OPENED());
						return true;
					}
					moveToInstance(player, (byte) 0);
				}
				break;
			case SELECT_NONE_1: // I'll take the safer path
			case SELECT_NONE_2: // Give me the dangerous path
				AIRequest request = new AIRequest() {

					@Override
					public void acceptRequest(Creature requester, Player responder, int requestId) {
						if (requestId == SM_QUESTION_WINDOW.STR_INSTANCE_DUNGEON_WITH_DIFFICULTY_ENTER_CONFIRM) {
							moveToInstance(responder, (byte) 2);
						} else {
							moveToInstance(responder, (byte) 1);
						}
					}

				};
				// 902051 = STR_INSTANCE_DUNGEON_DIFFICULTY_NORMAL (Normal), 902052 = STR_INSTANCE_DUNGEON_DIFFICULTY_HARD (Difficult)
				int pathL10nId = dialogActionId == SELECT_NONE_1 ? 902051 : 902052;
				AIActions.addRequest(this, player, SM_QUESTION_WINDOW.STR_INSTANCE_DUNGEON_WITH_DIFFICULTY_ENTER_CONFIRM, request, "300170000",
					ChatUtil.l10n(pathL10nId));
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
