package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.List;

import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.questEngine.handlers.models.Monster;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author MrPoke, Bobobear, Pad
 */
public class MentorMonsterHunt extends MonsterHunt {

	private int menteMinLevel;
	private int menteMaxLevel;

	public MentorMonsterHunt(int questId, List<Integer> startNpcIds, List<Integer> endNpcIds, List<Monster> monsters,
		int menteMinLevel, int menteMaxLevel, boolean reward, boolean rewardNextStep) {
		super(questId, startNpcIds, endNpcIds, monsters, 0, 0, null, 0, null, 0, reward, rewardNextStep);
		this.menteMinLevel = menteMinLevel;
		this.menteMaxLevel = menteMaxLevel;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			switch (DataManager.QUEST_DATA.getQuestById(questId).getMentorType()) {
				case MENTOR:
					if (player.isMentor()) {
						PlayerGroup group = player.getPlayerGroup();
						for (Player member : group.getMembers()) {
							if (member.getLevel() >= menteMinLevel && member.getLevel() <= menteMaxLevel
								&& PositionUtil.isInRange(player, member, GroupConfig.GROUP_MAX_DISTANCE)) {
								return super.onKillEvent(env);
							}
						}
					}
					break;
				case MENTE:
					if (player.isInGroup()) {
						PlayerGroup group = player.getPlayerGroup();
						for (Player member : group.getMembers()) {
							if (member.isMentor() && PositionUtil.isInRange(player, member, GroupConfig.GROUP_MAX_DISTANCE))
								return super.onKillEvent(env);
						}
					}
			}
		}
		return false;
	}
}
