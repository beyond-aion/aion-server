package quest.altgard;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * Talk with Gulkalla (203649). Destroy Hero Spirits (210588, 210722) (3). Go back to Gulkalla. Bring Umkata's Three
 * Tokens (700097) to Umkata's Grave (700098), summon the spirit of Umkata (210752, spawn), and destroy it. Report back
 * to Gulkalla.
 * 
 * @author Mr. Poke
 * @modified Gigi
 * @reworked vlog
 * @Modified Majka
 */
public class _2018ReconstructingImpetusium extends QuestHandler {

	private final static int questId = 2018;

	public _2018ReconstructingImpetusium() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(203649).addOnTalkEvent(questId);
		qe.registerQuestNpc(700097).addOnTalkEvent(questId);
		qe.registerQuestNpc(700098).addOnTalkEvent(questId);
		qe.registerQuestNpc(210588).addOnKillEvent(questId);
		qe.registerQuestNpc(210722).addOnKillEvent(questId);
		qe.registerQuestNpc(210752).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203649: { // Gulkalla
					switch (env.getDialog()) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							else if (var == 4) {
								return sendQuestDialog(env, 1352);
							}
							else if (var == 7) {
								return sendQuestDialog(env, 1693);
							}
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 4, 5); // 5
						}
						case SELECT_QUEST_REWARD: {
							changeQuestStep(env, 7, 7, true); // reward
							return sendQuestDialog(env, 5);
						}
					}
					break;
				}
				case 700097: { // Umkata's Jewel Box
					if (env.getDialog() == DialogAction.USE_OBJECT && var == 5) {
						return true; // loot
					}
					break;
				}
				case 700098: { // Umkata's Grave
					switch (env.getDialog()) {
						case USE_OBJECT: {
							if (var == 5) {
								return sendQuestDialog(env, 2034);
							}
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							if (QuestService.collectItemCheck(env, false)) { // don't remove yet
								QuestService.addNewSpawn(220030000, player.getInstanceId(), 210752, 2889.9834f, 1741.3108f, 254.75f,
									(byte) 0);
								return closeDialogWindow(env);
							}
							else {
								return sendQuestDialog(env, 2120);
							}
						}
						case FINISH_DIALOG: {
							return closeDialogWindow(env);
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203649) { // Gulkalla
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;
		int var = qs.getQuestVarById(0);
		if (var >= 1 && var < 4) {
			int[] npcs = { 210588, 210722 };
			return defaultOnKillEvent(env, npcs, var, var + 1); // 2 - 4
		}
		else if (var == 5) {
			if (env.getTargetId() == 210752) {
				qs.setQuestVar(7); // 7
				updateQuestStatus(env);
				QuestService.collectItemCheck(env, true); // now remove collected items
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 2200, true); // Sets as zone mission to avoid it appears on new player list.
	}
}
