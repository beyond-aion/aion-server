package quest.verteron;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Artur
 * @rework Ritsu
 */
public class _14014TurningTheIde extends QuestHandler {

	private final static int questId = 14014;

	public _14014TurningTheIde() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerQuestItem(182215314, questId);
		qe.registerQuestNpc(203146).addOnTalkEvent(questId);
		qe.registerQuestNpc(203147).addOnTalkEvent(questId);
		qe.registerQuestNpc(802045).addOnTalkEvent(questId);
		qe.registerQuestNpc(203164).addOnTalkEvent(questId);
		qe.registerQuestNpc(210178).addOnKillEvent(questId);
		qe.registerQuestNpc(216892).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		final int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203146:
					PacketSendUtility.sendMessage(player, env.getDialog().toString());
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
						case SETPRO1:
							ItemService.addItem(player, 182215314, 1);
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				case 203147:
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 1693);
						case SETPRO3:
							return defaultCloseDialog(env, 2, 3); // 3
					}
					break;
				case 802045:
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 3)
								return sendQuestDialog(env, 2034);
						case CHECK_USER_HAS_QUEST_ITEM:
							if (var == 3)
								return checkQuestItems(env, 3, 5, false, 2375, 2120);
					}
					break;
				case 203164:
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 8)
								return sendQuestDialog(env, 3057);
						case SELECT_QUEST_REWARD:
							return defaultCloseDialog(env, 8, 8, true, false);
					}
					break;
			}
		}

		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203164)
				return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		int[] mobs = { 210178, 216892 };
		return defaultOnKillEvent(env, mobs, 5, 7) || defaultOnKillEvent(env, mobs, 7, true);
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (qs.getQuestVarById(0) == 1 && player.isInsideZone(ZoneName.get("TURSIN_OUTPOST_210030000"))) {
				return HandlerResult.fromBoolean(useQuestItem(env, item, 1, 2, false, 18));
			}
		}
		return HandlerResult.FAILED;
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 14010, true);
	}
}
