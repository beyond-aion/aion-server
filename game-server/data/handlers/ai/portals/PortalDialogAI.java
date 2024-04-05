package ai.portals;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.List;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.DialogService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.findgroup.FindGroupService;
import com.aionemu.gameserver.services.teleport.PortalService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz, vlog
 */
@AIName("portal_dialog")
public class PortalDialogAI extends PortalAI {

	public PortalDialogAI(Npc owner) {
		super(owner);
	}

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
		if (getTalkDelayInMs() == 0) {
			checkDialog(player);
		} else {
			super.handleDialogStart(player);
		}
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		QuestEnv env = new QuestEnv(getOwner(), player, questId, dialogActionId);
		env.setExtendedRewardIndex(extendedRewardIndex);
		if (questId > 0 && QuestEngine.getInstance().onDialog(env)) {
			return true;
		}
		switch (dialogActionId) {
			case INSTANCE_PARTY_MATCH: // auto groups
				AutoGroupType agt = AutoGroupType.getAutoGroup(player.getLevel(), getNpcId());
				if (agt != null)
					PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(agt.getTemplate().getMaskId()));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
				return true;
			case OPEN_INSTANCE_RECRUIT:
				FindGroupService.getInstance().showInstanceGroups(player, getOwner());
				return true;
			case SELECT1_1:
				if (!player.isInTeam() && DataManager.AUTO_GROUP.getRecruitableInstanceMaskIds(getNpcId()) != null) {
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1182)); // show OPEN_INSTANCE_RECRUIT option
					return true;
				}
		}
		if (questId == 0) {
			PortalPath portalPath = DataManager.PORTAL2_DATA.getPortalDialogPath(getNpcId(), dialogActionId, player);
			if (portalPath != null)
				PortalService.port(portalPath, player, getOwner());
			return true;
		}
		return false;
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		checkDialog(player);
	}

	protected void checkDialog(Player player) {
		if (!DialogService.isInteractionAllowed(player, getOwner())) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
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
				} else if (qs == null || qs.isStartable()) {
					if (QuestService.checkStartConditions(player, questId, false)) {
						playerCanStartQuest = true;
					}
				}
			}
		}

		if (playerHasQuest) { // show quest selection dialog and handle teleportation in script, if needed
			boolean isRewardStep = false;
			for (int questId : relatedQuests) {
				QuestState qs = player.getQuestStateList().getQuestState(questId);
				if (qs != null && qs.getStatus() == QuestStatus.REWARD) { // reward dialog
					QuestEnv env = new QuestEnv(getOwner(), player, questId, USE_OBJECT);
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
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1012));
					break;
				case 730841:
				case 730883:
				case 804621:
				case 804624:
				case 804625:
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 4762));
					break;
				case 731583:
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10));
					break;
				case 731570:
					if (player.getRace() == Race.ASMODIANS) {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1352)); // seized danuar sanctuary
					} else {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011)); // danuar sanctuary
					}
					break;
				case 731549:
					if (player.getRace() == Race.ELYOS) {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011)); // seized danuar sanctuary
					} else {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1352)); // danuar sanctuary
					}
					break;
				default:
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), teleportationDialogId));
					break;
			}
		}
	}
}
