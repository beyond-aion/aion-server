package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _11077AWeaponOfWorth extends AbstractQuestHandler {

	public _11077AWeaponOfWorth() {
		super(11077);
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
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798926) {
				switch (dialogActionId) {
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
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1353);
						case SELECT2_1:
							return sendQuestDialog(env, 1353);
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1);
					}
					return false;
				}
				case 798918: // Pilipides
				{
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1693);
						case SELECT3_1:
							return sendQuestDialog(env, 1694);
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2);
					}
					return false;
				}
				case 798903: // Drenia
				{
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 2375);
						case SELECT_QUEST_REWARD:
							return defaultCloseDialog(env, 2, 3, true, true);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798903) { // Drenia
				switch (env.getDialogActionId()) {
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
