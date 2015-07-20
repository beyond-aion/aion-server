package quest.beluslan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 * @author Rhys2002
 * @modified & reworked Gigi
 */
public class _2051SavingBeluslanFortress extends QuestHandler {

	private final static int questId = 2051;
	private final static int[] npc_ids = { 204702, 204733, 204206, 278040, 700285, 700284 };

	public _2051SavingBeluslanFortress() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerQuestItem(182204302, questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 2500, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204702) {
				if (env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if (env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id())
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
			return false;
		}
		else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 204702) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
				case SETPRO1:
					return defaultCloseDialog(env, 0, 1);
			}
		}
		else if (targetId == 204733) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1352);
					else if (var == 2)
						return sendQuestDialog(env, 1693);
					else if (var == 6)
						return sendQuestDialog(env, 3057);
				case SETPRO2:
					return defaultCloseDialog(env, 1, 11);
				case SETPRO3:
					defaultCloseDialog(env, 2, 3);
					return TeleportService2.teleportTo(player, 120010000, 1368.81f, 1054.69f, 206.53f, (byte)89);
				case SETPRO7:
					return defaultCloseDialog(env, 6, 7);
			}
		}
		else if (targetId == 204206) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 3)
						return sendQuestDialog(env, 2034);
				case SETPRO4:
					return defaultCloseDialog(env, 3, 4);
			}
		}
		else if (targetId == 278040) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 4)
						return sendQuestDialog(env, 2375);
					else if (var == 5)
						return sendQuestDialog(env, 2716);
				case CHECK_USER_HAS_QUEST_ITEM:
					if (QuestService.collectItemCheck(env, true)) {
						if (!giveQuestItem(env, 182204302, 1))
							return true;
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						return sendQuestDialog(env, 10000);
					}
					else
						return sendQuestDialog(env, 10001);
				case SETPRO5:
					return defaultCloseDialog(env, 4, 5);
			}
		}
		else if (targetId == 700285) {
			switch (env.getDialog()) {
				case USE_OBJECT:
					if (var == 7)
						return useQuestObject(env, 7, 7, true, 0, 0, 0, 182204302, 1, 0, false); // reward
			}
		}
		else if (targetId == 700284) {
			switch (env.getDialog()) {
				case USE_OBJECT:
					if (var == 11)
						TeleportService2.teleportTo(player, 220040000, 370.05f, 427.5f, 222.11f, (byte)109);
						return useQuestObject(env, 11, 2, false, 0);
			}
		}
		return false;
	}
}
