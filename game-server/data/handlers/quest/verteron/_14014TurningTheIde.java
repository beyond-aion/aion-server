package quest.verteron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Artur, Ritsu, Majka
 */
public class _14014TurningTheIde extends AbstractQuestHandler {

	private final int[] mobs = { 210178, 216892 };

	public _14014TurningTheIde() {
		super(14014);
	}

	@Override
	public void register() {
		int[] questNpcs = { 203146, 203147, 802045, 203098 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestItem(182215314, questId);
		for (int questNpc : questNpcs)
			qe.registerQuestNpc(questNpc).addOnTalkEvent(questId);
		for (int mob : mobs)
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
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
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							return false;
						case SETPRO1:
							ItemService.addItem(player, 182215314, 1);
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				case 203147:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 1693);
							return false;
						case SETPRO3:
							return defaultCloseDialog(env, 2, 3); // 3
					}
					break;
				case 802045:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 3)
								return sendQuestDialog(env, 2034);
							return false;
						case CHECK_USER_HAS_QUEST_ITEM:
							if (var == 3)
								return checkQuestItems(env, 3, 5, false, 2375, 2120);
					}
					break;
				case 203098:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 8)
								return sendQuestDialog(env, 3057);
							return false;
						case SELECT_QUEST_REWARD:
							return defaultCloseDialog(env, 8, 8, true, false);
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203098)
				return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
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
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 14010);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 14010);
	}
}
