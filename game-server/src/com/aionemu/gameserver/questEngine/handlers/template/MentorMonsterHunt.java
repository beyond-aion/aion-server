package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.questEngine.handlers.models.Monster;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.MathUtil;

/**
 * @author MrPoke
 * @reworked Bobobear
 * @modified Pad
 */
public class MentorMonsterHunt extends MonsterHunt {

	private int menteMinLevel;
	private int menteMaxLevel;
	private QuestTemplate qt;

	/**
	 * @param questId
	 * @param startNpc
	 * @param endNpc
	 * @param monsters
	 */
	public MentorMonsterHunt(int questId, List<Integer> startNpcIds, List<Integer> endNpcIds, Map<Monster, Set<Integer>> monsters,
		int menteMinLevel, int menteMaxLevel, boolean reward, boolean rewardNextStep) {
		super(questId, startNpcIds, endNpcIds, monsters, 0, 0, null, 0, "NONE", 0, reward, rewardNextStep);
		this.menteMinLevel = menteMinLevel;
		this.menteMaxLevel = menteMaxLevel;
		this.qt = DataManager.QUEST_DATA.getQuestById(questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(getQuestId());
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			switch (qt.getMentorType()) {
				case MENTOR:
					if (player.isMentor()) {
						PlayerGroup group = player.getPlayerGroup2();
						for (Player member : group.getMembers()) {
							if (member.getLevel() >= menteMinLevel && member.getLevel() <= menteMaxLevel
								&& MathUtil.getDistance(player, member) < GroupConfig.GROUP_MAX_DISTANCE) {
								return super.onKillEvent(env);
							}
						}
					}
					break;
				case MENTE:
					if (player.isInGroup2()) {
						PlayerGroup group = player.getPlayerGroup2();
						for (Player member : group.getMembers()) {
							if (member.isMentor() && MathUtil.getDistance(player, member) < GroupConfig.GROUP_MAX_DISTANCE)
								return super.onKillEvent(env);
						}
					}
			}
		}
		return false;
	}
}
