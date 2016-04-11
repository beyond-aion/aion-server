package ai.portals;

import java.util.List;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FIND_GROUP;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.DialogService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.PortalService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 * @reworked vlog
 */
@AIName("portal_dialog")
public class PortalDialogAI2 extends PortalAI2 {

	/**
	 * Standard value. Can be changed through override
	 */
	protected int rewardDialogId = 5;
	/**
	 * Standard value. Can be changed through override
	 */
	protected int startingDialogId = 10;
	/**
	 * Standard value. Can be changed through override
	 */
	protected int questDialogId = 10;

	@Override
	protected void handleDialogStart(Player player) {
		if (getTalkDelay() == 0) {
			checkDialog(player);
		} else {
			super.handleDialogStart(player);
		}
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		QuestEnv env = new QuestEnv(getOwner(), player, questId, dialogId);
		env.setExtendedRewardIndex(extendedRewardIndex);
		if (questId > 0 && QuestEngine.getInstance().onDialog(env)) {
			return true;
		}
		if (dialogId == DialogAction.INSTANCE_PARTY_MATCH.id()) { // auto groups
			AutoGroupType agt = AutoGroupType.getAutoGroup(player.getLevel(), getNpcId());
			if (agt != null) {
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(agt.getInstanceMaskId()));
			}
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		} else if (dialogId == DialogAction.OPEN_INSTANCE_RECRUIT.id()) {
			AutoGroupType agt = AutoGroupType.getAutoGroup(player.getLevel(), getNpcId());
			if (agt != null) {
				PacketSendUtility.sendPacket(player, new SM_FIND_GROUP(0x1A, agt.getInstanceMapId()));
			}
		} else {
			if (dialogId == DialogAction.SELECT_ACTION_1012.id()) {
				AutoGroupType agt = AutoGroupType.getAutoGroup(player.getLevel(), getNpcId());
				if (agt != null) {
					if (agt.getPlayerSize() <= 6) {
						if (!player.isInGroup2()) {
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1182));
							return true;
						}
					} else {
						if (!player.isInAlliance2()) {
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1182));
							return true;
						}
					}
				}
			}
			if (questId == 0) {
				PortalPath portalPath = DataManager.PORTAL2_DATA.getPortalDialog(getNpcId(), dialogId, player.getRace());
				if (portalPath != null) {
					if (portalPath.getMinRank() > player.getAbyssRank().getRank().getId()) {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogPage.NO_RIGHT.id(), questId));
					} else
						PortalService.port(portalPath, player, getObjectId());
				}
			} else {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), dialogId, questId));
			}
		}
		return true;
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		checkDialog(player);
	}

	protected void checkDialog(Player player) {
		if (DialogService.isSubDialogRestricted(startingDialogId, player, getOwner())) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogAction.SELECT_ACTION_1011.id()));
			return;
		}

		int npcId = getNpcId();
		int teleportationDialogId = DataManager.PORTAL2_DATA.getTeleportDialogId(npcId);
		List<Integer> relatedQuests = QuestEngine.getInstance().getQuestNpc(npcId).getOnTalkEvent();
		boolean playerHasQuest = false;
		boolean playerCanStartQuest = false;
		if (!relatedQuests.isEmpty()) {
			for (int questId : relatedQuests) {
				QuestState qs = player.getQuestStateList().getQuestState(questId);
				if (qs != null && (qs.getStatus() == QuestStatus.START || qs.getStatus() == QuestStatus.REWARD)) {
					playerHasQuest = true;
					break;
				} else if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
					if (QuestService.checkStartConditions(new QuestEnv(getOwner(), player, questId, 0), false)) {
						playerCanStartQuest = true;
						continue;
					}
				}
			}
		}

		if (playerHasQuest) { // show quest selection dialog and handle teleportation in script, if needed
			boolean isRewardStep = false;
			for (int questId : relatedQuests) {
				QuestState qs = player.getQuestStateList().getQuestState(questId);
				if (qs != null && qs.getStatus() == QuestStatus.REWARD) { // reward dialog
					QuestEnv env = new QuestEnv(getOwner(), player, questId, DialogAction.USE_OBJECT.id());
					isRewardStep = QuestEngine.getInstance().onDialog(env);
					if (isRewardStep)
						break;
				}
			}
			if (!isRewardStep) // normal dialog
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), questDialogId));
		} else if (playerCanStartQuest) { // start quest dialog
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), startingDialogId));
		} else { // show teleportation dialog
			switch (npcId) {
				case 831117:
				case 831131:
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1012, 0));
					break;
				case 730841:
				case 730883:
				case 804621:
				case 804624:
				case 804625:
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 4762, 0));
					break;
				case 731583:
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10, 0));
					break;
				case 731570:
					if (player.getRace() == Race.ASMODIANS) {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1352, 0)); // seized danuar sanctuary
					} else {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011, 0)); // danuar sanctuary
					}
					break;
				case 731549:
					if (player.getRace() == Race.ELYOS) {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011, 0)); // seized danuar sanctuary
					} else {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1352, 0)); // danuar sanctuary
					}
					break;
				default:
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), teleportationDialogId, 0));
					break;
			}
		}
	}
}
