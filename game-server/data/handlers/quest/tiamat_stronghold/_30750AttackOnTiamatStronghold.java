package quest.tiamat_stronghold;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * Not sure about second to third step "Secure the way to the Dredgion Control Center."
 * For now, we consider Chantra's death, which opens the door leading to the mentioned room, as quest progress trigger.
 *
 * @author Estrayl
 */
public class _30750AttackOnTiamatStronghold extends AbstractQuestHandler {

	private static final int ENTER_WORLD_ID = 300510000; // Tiamat_Stronghold
	private static final int START_NPC_ID = 804869; // Ginnie
	private static final int TALK_NPC_ID = 800368; // Frightening Vhasha
	private static final int KAHRUN_NPC_ID = 800338; // Kahrun
	private static final int SHABOKAN_NPC_ID = 219352; // Invincible Shabokan
	private static final int TAHABATA_NPC_ID = 219358; // Brigade General Tahabata
	private static final int CHANTRA_NPC_ID = 219353; // Brigade General Chantra

	public _30750AttackOnTiamatStronghold() {
		super(30750);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(START_NPC_ID).addOnQuestStart(questId);
		qe.registerQuestNpc(START_NPC_ID).addOnTalkEvent(questId);
		qe.registerQuestNpc(TALK_NPC_ID).addOnTalkEvent(questId);
		qe.registerQuestNpc(KAHRUN_NPC_ID).addOnTalkEvent(questId);
		qe.registerQuestNpc(SHABOKAN_NPC_ID).addOnKillEvent(questId);
		qe.registerQuestNpc(CHANTRA_NPC_ID).addOnKillEvent(questId);
		qe.registerQuestNpc(TAHABATA_NPC_ID).addOnKillEvent(questId);
		qe.registerOnEnterWorld(questId);

	}

	public boolean onDialogEvent(QuestEnv env) {
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == START_NPC_ID) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == TALK_NPC_ID && qs.getQuestVarById(0) == 3) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 2034);
				} else if (dialogActionId == SETPRO4) {
					changeQuestStep(env, 3, 4);
					return closeDialogWindow(env);
				}
			} else if (targetId == KAHRUN_NPC_ID && qs.getQuestVarById(0) == 5) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 2716);
				} else if (dialogActionId == SET_SUCCEED) {
					changeQuestStep(env, 5, 5, true);
					return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == START_NPC_ID) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0 && player.getWorldId() == ENTER_WORLD_ID) {
			changeQuestStep(env, 0, 1);
			return true;
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int targetId = env.getTargetId();
		if (targetId == SHABOKAN_NPC_ID && qs.getQuestVarById(0) == 1) {
			changeQuestStep(env, 1, 2);
			return true;
		} else if (targetId == CHANTRA_NPC_ID && qs.getQuestVarById(0) == 2) {
			changeQuestStep(env, 2, 3);
			return true;
		} else if (targetId == TAHABATA_NPC_ID && qs.getQuestVarById(0) == 4) {
			changeQuestStep(env, 4, 5);
			return true;
		}
		return false;
	}
}
