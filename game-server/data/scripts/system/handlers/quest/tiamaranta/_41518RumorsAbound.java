package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Cheatkiller
 */
public class _41518RumorsAbound extends QuestHandler {

	private final static int questId = 41518;

	public _41518RumorsAbound() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerCanAct(questId, 701260);
		qe.registerQuestNpc(205938).addOnQuestStart(questId);
		qe.registerQuestNpc(205938).addOnTalkEvent(questId);
		qe.registerQuestNpc(701260).addOnTalkEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("SUSPICIOUS_VILLAGE_ENTRANCE_600030000"), questId);
		qe.registerOnEnterZone(ZoneName.get("ARACHI_FLOATING_ISLAND_600030000"), questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205938) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 4762);
				} else if (dialog == DialogAction.QUEST_ACCEPT_SIMPLE) {
					giveQuestItem(env, 182212588, 1);
					return sendQuestStartDialog(env);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 701260) {
				QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 218731, player.getX() + 2, player.getY() + 2, player.getZ(), (byte) 0);
				QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 218732, player.getX() - 2, player.getY() - 2, player.getZ(), (byte) 0);
				changeQuestStep(env, 0, 1, false);
				return true;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205938) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 10002);
					}
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		Player player = env.getPlayer();
		if (player == null)
			return false;
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (zoneName == ZoneName.get("SUSPICIOUS_VILLAGE_ENTRANCE_600030000")) {
				if (var == 2) {
					changeQuestStep(env, 2, 2, true);
					return true;
				}
			} else if (zoneName == ZoneName.get("ARACHI_FLOATING_ISLAND_600030000")) {
				if (var == 1) {
					changeQuestStep(env, 1, 2, false);
					return true;
				}
			}
		}
		return false;
	}
}
