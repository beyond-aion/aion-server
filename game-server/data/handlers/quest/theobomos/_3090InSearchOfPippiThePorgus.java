package quest.theobomos;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Cheatkiller
 */
public class _3090InSearchOfPippiThePorgus extends AbstractQuestHandler {

	public _3090InSearchOfPippiThePorgus() {
		super(3090);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798182).addOnQuestStart(questId);
		qe.registerQuestNpc(798182).addOnTalkEvent(questId);
		qe.registerQuestNpc(798193).addOnTalkEvent(questId);
		qe.registerQuestNpc(700420).addOnTalkEvent(questId);
		qe.registerQuestNpc(700421).addOnTalkEvent(questId);
		qe.registerQuestNpc(206085).addOnAtDistanceEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798182) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798193) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0) {
						return sendQuestDialog(env, 1011);
					} else if (qs.getQuestVarById(0) == 2) {
						return sendQuestDialog(env, 1693);
					}
				} else if (dialogActionId == SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				} else if (dialogActionId == SETPRO3) {
					if (player.getInventory().getKinah() >= 10000) {
						giveQuestItem(env, 182208050, 1);
						player.getInventory().decreaseKinah(10000);
						return defaultCloseDialog(env, 2, 3);
					} else
						return sendQuestDialog(env, 1779);
				} else if (dialogActionId == SELECT3_2) {
					return sendQuestDialog(env, 1779);
				}
			}
			if (targetId == 700420) {
				int var = qs.getQuestVarById(0);
				int var2 = qs.getQuestVarById(2);
				if (var == 1 && var2 == 0) {
					changeQuestStep(env, 0, 1, false, 2);
					PacketSendUtility.sendMonologue(player, 1111007);
					changeStep(qs, env);
					return true;
				}
			}
			if (targetId == 700421) {
				if (dialogActionId == USE_OBJECT) {
					if (qs.getQuestVarById(0) == 3) {
						return sendQuestDialog(env, 2034);
					}
				} else if (dialogActionId == SET_SUCCEED) {
					env.getVisibleObject().getController().deleteAndScheduleRespawn();
					removeQuestItem(env, 182208050, 1);
					giveQuestItem(env, 182208051, 1);
					return defaultCloseDialog(env, 3, 3, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798182) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 10002);
					default: {
						removeQuestItem(env, 182208051, 1);
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onAtDistanceEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			int var1 = qs.getQuestVarById(1);
			if (var == 1 && var1 == 0) {
				changeQuestStep(env, 0, 1, false, 1);
				PacketSendUtility.sendMonologue(player, 1111006);
				changeStep(qs, env);
				return true;
			}
		}
		return false;
	}

	private void changeStep(QuestState qs, QuestEnv env) {
		if (qs.getQuestVarById(1) == 1 && qs.getQuestVarById(2) == 1) {
			qs.setQuestVarById(1, 0);
			qs.setQuestVarById(2, 0);
			qs.setQuestVar(2);
			updateQuestStatus(env);
		}
	}
}
