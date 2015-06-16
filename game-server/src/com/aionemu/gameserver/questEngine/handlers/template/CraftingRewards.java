package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_LIST;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.craft.CraftSkillUpdateService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Bobobear
 */
public class CraftingRewards extends QuestHandler {

	private final int startNpcId;
	private final int skillId;
	private final int levelReward;
	private final int questMovie;
	private final int endNpcId;

	public CraftingRewards(int questId, int startNpcId, int skillId, int levelReward, int endNpcId, int questMovie) {
		super(questId);
		this.startNpcId = startNpcId;
		this.skillId = skillId;
		this.levelReward = levelReward;
		if (endNpcId != 0) {
			this.endNpcId = endNpcId;
		}
		else {
			this.endNpcId = startNpcId;
		}
		this.questMovie = questMovie;
	}

	@Override
	public void register() {
		qe.registerQuestNpc(startNpcId).addOnQuestStart(questId);
		qe.registerQuestNpc(startNpcId).addOnTalkEvent(questId);
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
			if (!canLearn(player) && playerSkillLevel != levelReward) {
				return false;
			}
		}

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == startNpcId) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1011);
					}
					default: {
						return sendQuestStartDialog(env);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == endNpcId) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 2375);
					}
					case SELECT_QUEST_REWARD: {
						qs.setQuestVar(0);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						if (questMovie != 0) {
							playQuestMovie(env, questMovie);
						}
						else {
							player.getSkillList().addSkill(player, skillId, levelReward);
						}
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == endNpcId) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestEndDialog(env);
					}
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}

	private boolean canLearn(Player player) {
		return levelReward == 400 ? CraftSkillUpdateService.canLearnMoreExpertCraftingSkill(player)
			: levelReward == 500 ? CraftSkillUpdateService.canLearnMoreMasterCraftingSkill(player) : true;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs.getStatus() == QuestStatus.REWARD) {
			if (movieId == questMovie && canLearn(player)) {
				player.getSkillList().addSkill(player, skillId, levelReward);
				player.getRecipeList().autoLearnRecipe(player, skillId, levelReward);
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 1330064, false));
				return true;
			}
		}
		return false;
	}

	@Override
	public HashSet<Integer> getNpcIds() {
		if (constantSpawns == null) {
			constantSpawns = new HashSet<>();
			constantSpawns.add(startNpcId);
			if (endNpcId != 0)
				constantSpawns.add(endNpcId);
		}
		return constantSpawns;
	}
}
