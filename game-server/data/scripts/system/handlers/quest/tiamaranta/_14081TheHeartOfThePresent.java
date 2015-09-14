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
 * @author Artur
 */
public class _14081TheHeartOfThePresent extends QuestHandler {

	private final static int questId = 14081;
	private final static int[] npc_ids = { 802059, 702090 };

	public _14081TheHeartOfThePresent() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(218766).addOnKillEvent(questId);
		qe.registerQuestNpc(233869).addOnKillEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("LDF4B_SENSORYAREA_Q14081_206351_6_600030000"), questId);
		qe.registerQuestItem(182215404, questId);
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
							if (!giveQuestItem(env, 182215404, 1))
								qs.setQuestVar(1);
							updateQuestStatus(env);
							return defaultCloseDialog(env, 0, 1);
						}
					}
					break;
				case 702090:// Exploding Rock (ToDo dont know how to handle that)
					switch (dialog) {
						case USE_OBJECT: {
							if (env.getDialog() == DialogAction.USE_OBJECT)
								return sendQuestDialog(env, 10002);
							else
								return sendQuestEndDialog(env);
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							if (!giveQuestItem(env, 182215404, 1))
								return true;
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 802059) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 2716);
				} else {
					return sendQuestEndDialog(env);
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
			if (targetId == 233869) {// Source Protector
				QuestService.addNewSpawn(600030000, player.getInstanceId(), 218766, player.getX(), player.getY(), player.getZ(),
					(byte) 95);// Fragmented Pyreogre
				return defaultOnKillEvent(env, 233869, 2, 3);
			}
			if (targetId == 218766) { // Fragmented Pyreogre
				QuestService.addNewSpawn(600030000, player.getInstanceId(), 702090, player.getX(), player.getY(), player.getZ(),
					(byte) 95);// Exploding Rock
				return defaultOnKillEvent(env, 218766, 3, 4);
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
			if (player.isInsideZone(ZoneName.get("LDF4B_ITEMUSEAREA_Q10060A"))) {
				if (var == 1) {
					QuestService.addNewSpawn(600030000, player.getInstanceId(), 702314, player.getX(), player.getY(), player.getZ(),
						(byte) 95);
					return HandlerResult.fromBoolean(useQuestItem(env, item, 1, 2, false));
				}
			}
		}
		return HandlerResult.SUCCESS;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName == ZoneName.get("LDF4B_SENSORYAREA_Q14081_206351_6_600030000")) {
			Player player = env.getPlayer();
			if (player == null)
				return false;
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);
				if (var == 4) {
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 14080);
	}
}
