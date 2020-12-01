package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author vlog
 */
public class _1607MappingTheRevolutionaries extends AbstractQuestHandler {

	public _1607MappingTheRevolutionaries() {
		super(1607);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182201744, questId);
		qe.registerQuestNpc(204578).addOnTalkEvent(questId);
		qe.registerQuestNpc(204574).addOnTalkEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("MUDTHORN_EXPERIMENT_LAB_210040000"), questId);
		qe.registerOnEnterZone(ZoneName.get("ROTRON_EXPERIMENT_LAB_210040000"), questId);
		qe.registerOnEnterZone(ZoneName.get("PRETOR_EXPERIMENT_LAB_210040000"), questId);
		qe.registerOnEnterZone(ZoneName.get("POISON_EXTRACTION_LAB_210040000"), questId);
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (QuestService.startQuest(env)) {
				return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
			}
		}
		return HandlerResult.FAILED;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();
		if (qs == null) {
			return false;
		}

		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			int var1 = qs.getQuestVarById(1);
			int var2 = qs.getQuestVarById(2);
			int var3 = qs.getQuestVarById(3);
			int var4 = qs.getQuestVarById(4);
			if (targetId == 204578) { // Kuobe
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1); // 1
				}
			} else if (targetId == 204574) { // Finn
				if (dialogActionId == QUEST_SELECT) {
					if (var == 1 && var1 == 1 && var2 == 1 && var3 == 1 && var4 == 1) {
						return sendQuestDialog(env, 10002);
					}
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					changeQuestStep(env, 1, 1, true);
					return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204574) { // Finn
				return sendQuestEndDialog(env);
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
			int var1 = qs.getQuestVarById(1);
			int var2 = qs.getQuestVarById(2);
			int var3 = qs.getQuestVarById(3);
			int var4 = qs.getQuestVarById(4);
			if (var == 1) {
				if (zoneName == ZoneName.get("MUDTHORN_EXPERIMENT_LAB_210040000")) {
					if (var1 == 0) {
						changeQuestStep(env, 0, 1, false, 1); // 1: 1
						return true;
					}
				} else if (zoneName == ZoneName.get("ROTRON_EXPERIMENT_LAB_210040000")) {
					if (var2 == 0) {
						changeQuestStep(env, 0, 1, false, 2); // 2: 1
						return true;
					} else if (zoneName == ZoneName.get("PRETOR_EXPERIMENT_LAB_210040000")) {
						if (var3 == 0) {
							changeQuestStep(env, 0, 1, false, 3); // 3: 1
							return true;
						}
					} else if (zoneName == ZoneName.get("POISON_EXTRACTION_LAB_210040000")) {
						if (var4 == 0) {
							changeQuestStep(env, 0, 1, false, 4); // 4: 1
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
