package quest.inggison;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _11077AWeaponOfWorth extends QuestHandler {

	private final static int questId = 11077;

	public _11077AWeaponOfWorth() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798926).addOnQuestStart(questId); // Outremus
		qe.registerQuestNpc(798926).addOnTalkEvent(questId); // Outremus
		qe.registerQuestNpc(799028).addOnTalkEvent(questId); // Brontes
		qe.registerQuestNpc(798918).addOnTalkEvent(questId); // Pilipides
		qe.registerQuestNpc(798903).addOnTalkEvent(questId); // Drenia
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798926) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_1:
						if (giveQuestItem(env, 182214016, 1))
							return sendQuestStartDialog(env);
						return true;
					default: {

						return sendQuestStartDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 799028: // Brontes
				{
					switch (dialog) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1353);
						case SELECT_ACTION_1353:
							return sendQuestDialog(env, 1353);
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1);
					}
					return false;
				}
				case 798918: // Pilipides
				{
					switch (dialog) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1693);
						case SELECT_ACTION_1694:
							return sendQuestDialog(env, 1694);
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2);
					}
					return false;
				}
				case 798903: // Drenia
				{
					switch (dialog) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 2375);
						case SELECT_QUEST_REWARD:
							return defaultCloseDialog(env, 2, 3, true, true);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798903) // Drenia
			{
				switch (env.getDialogId()) {
					case 1009:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
