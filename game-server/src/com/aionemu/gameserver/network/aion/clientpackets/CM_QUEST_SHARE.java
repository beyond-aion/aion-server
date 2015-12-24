package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.QuestTarget;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ginho1
 * @modified Neon
 */
public class CM_QUEST_SHARE extends AionClientPacket {

	public int questId;

	public CM_QUEST_SHARE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		this.questId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		QuestTemplate questTemplate = DataManager.QUEST_DATA.getQuestById(questId);
		QuestState questState = player.getQuestStateList().getQuestState(questId);

		if (questTemplate == null || questTemplate.isCannotShare()) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1100001)); // quest cannot be shared
			return;
		}

		if ((questState == null) || (questState.getStatus() == QuestStatus.COMPLETE))
			return;

		if (!player.isInAlliance2() && questTemplate.getTarget() == QuestTarget.ALLIANCE) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1100005, player.getName())); // no alliance member to share the quest with
			return;
		}

		if (!player.isInTeam()) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1100000)); // no group member to share the quest with
			return;
		}

		for (Player member : player.isInGroup2() ? player.getPlayerGroup2().getOnlineMembers() : player.getPlayerAllianceGroup2().getOnlineMembers()) {
			if (player.getObjectId().equals(member.getObjectId()) || !MathUtil.isIn3dRange(member, player, GroupConfig.GROUP_MAX_DISTANCE))
				continue;

			if (member.getQuestStateList().getQuestState(questId) != null) {
				QuestStatus qs = member.getQuestStateList().getQuestState(questId).getStatus();
				if (!questTemplate.isRepeatable() && qs != QuestStatus.NONE)
					continue;
				else if (qs == QuestStatus.START || qs == QuestStatus.REWARD)
					continue;
			}

			if (!QuestService.checkLevelRequirement(questId, member.getLevel())) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1100003, member.getName()));
			} else {
				PacketSendUtility.sendPacket(member, new SM_QUEST_ACTION(questId, member.getObjectId(), true));
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1100002, member.getName()));
			}
		}
	}
}
