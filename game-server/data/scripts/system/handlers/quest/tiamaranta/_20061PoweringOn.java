package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author zhkchi
 */
public class _20061PoweringOn extends QuestHandler {

	private final static int questId = 20061;

	public _20061PoweringOn() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205886).addOnQuestStart(questId);
		qe.registerQuestNpc(205886).addOnTalkEvent(questId);
		qe.registerQuestNpc(800018).addOnTalkEvent(questId);
		qe.registerQuestNpc(218767).addOnKillEvent(questId);
		qe.registerQuestNpc(701233).addOnTalkEvent(questId);
		qe.registerGetingItem(182212558, questId);
		qe.registerOnEnterZone(ZoneName.get("GRAVITY_WELL_600030000"), questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnLevelUp(questId);
	}
	
	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		int[] quests = { 20060 };
		return defaultOnLvlUpEvent(env, quests, true);
	}
	
	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			return QuestService.startQuest(env);
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {

		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205886) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case SELECT_ACTION_1012:
						return sendQuestDialog(env, 1012);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
				}
			}else if(targetId == 701233){
				if(qs.getQuestVarById(0) == 3){
					ItemService.addItem(player, 182212558, 1);
					env.getVisibleObject().getController().onDelete();
					return true;
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800018) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName == ZoneName.get("GRAVITY_WELL_600030000")) {
			Player player = env.getPlayer();
			if (player == null)
				return false;
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);
				if (var == 1) {
					changeQuestStep(env, 1, 2, false);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START && qs.getQuestVarById(0) != 2)
			return false;

		int targetId = 0;
		
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (targetId == 218767) {
			changeQuestStep(env, 2, 3, false);
		}
		QuestService.addNewSpawn(600030000, player.getInstanceId(), 701233, 1712.9526f, 514.41455f, 200.16928f, (byte) 76);
		return false;
	}

	@Override
	public boolean onGetItemEvent(QuestEnv env) {
		return defaultOnGetItemEvent(env, 3, 3, true);
	}
}
