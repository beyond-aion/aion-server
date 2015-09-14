package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Enomine
 */

public class _24081SecretsWithinTheHeart extends QuestHandler {

	private final static int questId = 24081;
	private final static int[] npc_ids = { 802059, 701233 };

	public _24081SecretsWithinTheHeart() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(218767).addOnKillEvent(questId);
		qe.registerQuestNpc(233870).addOnKillEvent(questId);
		qe.registerQuestNpc(206351).addOnAtDistanceEvent(questId);
		qe.registerQuestItem(182215406, questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		if (qs == null)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 802059:// Protector Oriata
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1011);
						}
						case SETPRO1: {
							if (!giveQuestItem(env, 182215406, 1))
								return true;
							qs.setQuestVar(1);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
					}
					break;
				case 701233:// Gravity Fault
					switch (dialog) {
						case USE_OBJECT: {
							return sendQuestDialog(env, 1693);
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							if (!giveQuestItem(env, 182215407, 1))
								return true;
						}
						case SETPRO3: {
							qs.setQuestVar(3);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
					}
			}
		}
		if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 802059) {// Portector Oriata
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 2716);
					}
					case SELECT_QUEST_REWARD: {
						return sendQuestDialog(env, 5);
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
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int targetId = env.getTargetId();
			if (targetId == 218767) { // fragmented Gapermaw
				QuestService.addNewSpawn(600030000, player.getInstanceId(), 701233, player.getX(), player.getY(), player.getZ(), (byte) 95);// Gravity Fault
				return defaultOnKillEvent(env, 218767, 2, 2);
			}
			if (targetId == 233870) {// Heart of Fissure Guardian
				return defaultOnKillEvent(env, 233870, 3, 4);
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
			if (var == 4) {
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int var = qs.getQuestVarById(0);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.isInsideZone(ZoneName.get("LDF4B_ITEMUSEAREA_Q20062A"))) {
				if (var == 1) {
					QuestService.addNewSpawn(600030000, player.getInstanceId(), 702314, player.getX(), player.getY(), player.getZ(), (byte) 95);
					return HandlerResult.fromBoolean(useQuestItem(env, item, 1, 2, false));
				}
			}
		}
		return HandlerResult.SUCCESS;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		int[] quests = { 24071 };
		return defaultOnLvlUpEvent(env, quests, true);
	}

}
