package quest.fort_tiamat;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Cheatkiller
 */
public class _20071TheHuntForIsraphel extends QuestHandler {

	private final static int questId = 20071;

	public _20071TheHuntForIsraphel() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 205579, 205617, 730465, 295987, 730465 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnLogOut(questId);
		qe.registerOnQuestTimerEnd(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205579) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						}
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1); // 1
					}
				}
			} else if (targetId == 205617) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 1) {
							return sendQuestDialog(env, 1352);
						}
					}
					case SETPRO2: {
						if (giveQuestItem(env, 182213249, 1))
							return defaultCloseDialog(env, 1, 2);
					}
				}
			} else if (targetId == 205987) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 2) {
							return sendQuestDialog(env, 1693);
						} else if (var == 4)
							return sendQuestDialog(env, 2375);
						else if (var == 5)
							return sendQuestDialog(env, 2716);
					}
					case SETPRO3: {
						removeQuestItem(env, 182213249, 1);
						return defaultCloseDialog(env, 2, 3);
					}
					case SETPRO5: {
						player.getEffectController().removeEffect(2785);
						QuestService.questTimerEnd(env);
						return defaultCloseDialog(env, 4, 5);
					}
					case SET_SUCCEED: {
						giveQuestItem(env, 182213250, 1);
						return defaultCloseDialog(env, 5, 6, true, false);
					}
				}
			} else if (targetId == 730465) { // Mysterious Orb
				switch (dialog) {
					case USE_OBJECT: {
						if (var == 3) {
							SkillEngine.getInstance().applyEffectDirectly(2785, player, player, 0);
							changeQuestStep(env, 3, 4, false);
							QuestService.questTimerStart(env, 180);
							return true;
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205617) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(4);
			if (var == 4) {
				qs.setQuestVar(3);
				updateQuestStatus(env);
				player.getEffectController().removeEffect(2785);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 4) {
				qs.setQuestVar(3);
				updateQuestStatus(env);
				player.getEffectController().removeEffect(2785);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 20070);
	}
}
