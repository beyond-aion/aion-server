package com.aionemu.gameserver.questEngine.handlers.template;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.craft.CraftSkillUpdateService;

/**
 * @author Bobobear
 * @modified Pad
 */
public class CraftingRewards extends QuestHandler {

	private final int startNpcId, endNpcId;
	private final int skillId;
	private final int levelReward;
	private final int questMovie;
	private final boolean isDataDriven;

	public CraftingRewards(int questId, int startNpcId, int skillId, int levelReward, int endNpcId, int questMovie) {
		super(questId);
		this.startNpcId = startNpcId;
		this.endNpcId = endNpcId != 0 ? endNpcId : startNpcId;
		this.skillId = skillId;
		this.levelReward = levelReward;
		this.questMovie = questMovie;
		isDataDriven = DataManager.QUEST_DATA.getQuestById(questId).isDataDriven();
	}

	@Override
	public void register() {
		if (startNpcId != 0) {
			qe.registerQuestNpc(startNpcId).addOnQuestStart(questId);
			qe.registerQuestNpc(startNpcId).addOnTalkEvent(questId);
		}
		if (questMovie != 0) {
			qe.registerOnMovieEndQuest(questMovie, questId);
		}
		if (endNpcId != startNpcId) {
			qe.registerQuestNpc(endNpcId).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		PlayerSkillEntry skill = player.getSkillList().getSkillEntry(skillId);

		if (skill != null) {
			int playerSkillLevel = skill.getSkillLevel();
			if (dialog == DialogAction.QUEST_SELECT) {
				if (playerSkillLevel != levelReward && !canLearn(player)) {
					return sendQuestSelectionDialog(env);
				}
			}
		}
		if (qs == null || qs.isStartable()) {
			if (targetId == startNpcId) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, isDataDriven ? 4762 : 1011);
					default: {
						return sendQuestStartDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == endNpcId) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, isDataDriven ? 1011 : 2375);
					case SELECT_QUEST_REWARD:
						qs.setQuestVar(0);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						if (questMovie != 0) {
							playQuestMovie(env, questMovie);
						} else {
							player.getSkillList().addSkill(player, skillId, levelReward);
						}
						return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == endNpcId) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	private boolean canLearn(Player player) {
		return levelReward == 400 ? CraftSkillUpdateService.getInstance().canLearnMoreExpertCraftingSkill(player)
			: levelReward == 500 ? CraftSkillUpdateService.getInstance().canLearnMoreMasterCraftingSkill(player) : true;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs.getStatus() == QuestStatus.REWARD) {
			if (movieId == questMovie && canLearn(player)) {
				player.getSkillList().addSkill(player, skillId, levelReward);
				return true;
			}
		}
		return false;
	}
}
