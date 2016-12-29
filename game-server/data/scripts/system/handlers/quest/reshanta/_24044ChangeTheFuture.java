package quest.reshanta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Artur
 * @modified Majka
 */
public class _24044ChangeTheFuture extends QuestHandler {

	private final static int questId = 24044;

	public _24044ChangeTheFuture() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(278036).addOnTalkEvent(questId);
		qe.registerQuestNpc(203550).addOnTalkEvent(questId);
		qe.registerQuestNpc(204207).addOnTalkEvent(questId);
		qe.registerQuestNpc(798067).addOnTalkEvent(questId);
		qe.registerQuestNpc(279029).addOnTalkEvent(questId);
		qe.registerQuestNpc(700355).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		if (qs == null)
			return false;
		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 278036: // Scoda
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				case 203550: // Munin
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2); // 2
					}
					break;
				case 204207: // Kasir
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
							return false;
						case SETPRO3:
							return defaultCloseDialog(env, 2, 3); // 3
					}
					break;
				case 798067: // Lyeanenerk
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
							return false;
						case SETPRO4:
							return defaultCloseDialog(env, 3, 4); // 4
					}
					break;
				case 279029: // Lugbug
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 4) {
								return sendQuestDialog(env, 2375);
							} else if (var == 6) {
								return sendQuestDialog(env, 3057);
							}
							return false;
						case SETPRO5:
							return defaultCloseDialog(env, 4, 5); // 5
						case SET_SUCCEED:
							return defaultCloseDialog(env, 6, 6, true, false); // reward
					}
					break;
				case 700355: // Artifact of the Inception
					switch (dialog) {
						case USE_OBJECT:
							if (var == 5) {
								if (player.getInventory().getItemCountByItemId(188020000) > 0) {
									return useQuestObject(env, 5, 6, false, 0, 0, 0, 188020000, 1, 291, false); // 6
								}
								PacketSendUtility.sendMonologue(player, 1111203); // I need an Artifact Activation Stone!
							}
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 278036) { // Scoda
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 24040);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 24040);
	}
}
