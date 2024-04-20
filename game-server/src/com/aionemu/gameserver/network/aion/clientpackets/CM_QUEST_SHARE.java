package com.aionemu.gameserver.network.aion.clientpackets;

import static com.aionemu.gameserver.utils.collections.Predicates.Players.ONLINE;
import static com.aionemu.gameserver.utils.collections.Predicates.Players.allExcept;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.QuestTarget;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author ginho1, Neon
 */
public class CM_QUEST_SHARE extends AionClientPacket {

	private int questId;

	public CM_QUEST_SHARE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		this.questId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		QuestTemplate questTemplate = DataManager.QUEST_DATA.getQuestById(questId);
		if (questTemplate == null || questTemplate.isCannotShare()) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1100001)); // This quest cannot be shared.
			return;
		}

		QuestState questState = player.getQuestStateList().getQuestState(questId);
		if (questState == null || questState.getStatus() == QuestStatus.COMPLETE)
			return;

		List<Player> membersToShareWith;
		TemporaryPlayerTeam<? extends TeamMember<Player>> currentGroup = player.getCurrentGroup();
		if (currentGroup == null) {
			membersToShareWith = Collections.emptyList();
		} else {
			Predicate<Player> memberFilter = allExcept(player).and(ONLINE).and(member -> PositionUtil.isInRange(member, player, GroupConfig.GROUP_MAX_DISTANCE));
			membersToShareWith = currentGroup.filterMembers(memberFilter);
		}
		if (membersToShareWith.isEmpty()) {
			if (questTemplate.getTarget() == QuestTarget.ALLIANCE) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1100005)); // There are no Alliance members to share the quest with.
			} else {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1100000)); // There are no group members to share the quest with.
			}
			return;
		}

		for (Player member : membersToShareWith) {
			if (!QuestService.checkStartConditions(member, questId, false)) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1100003, member.getName())); // You failed to share the quest with %0.
			} else {
				PacketSendUtility.sendPacket(member, new SM_QUEST_ACTION(questId, player.getObjectId(), member.isInAlliance()));
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1100002, member.getName())); // You shared the quest with %0.
			}
		}
	}
}
