package quest.beluslan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Ritsu
 * #Modified Majka
 */
public class _24051InvesetigatetheDisappearance extends QuestHandler {

	private final static int questId = 24051;

	public _24051InvesetigatetheDisappearance() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 204707, 204749, 204800, 700359 };
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerQuestItem(182215375, questId);
		qe.registerOnMovieEndQuest(236, questId);
		qe.registerOnEnterZone(ZoneName.get("MINE_PORT_220040000"), questId);
		qe.registerOnEnterWorld(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 204707) { // Mani
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						}
						else if (var == 3){
							return sendQuestDialog(env, 2034);
						}
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1); // 1
					}
					case SETPRO4: {
						return defaultCloseDialog(env, 3, 4); // 4
					}
				}
			}
			else if (targetId == 204749) { // Paeru
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 1) {
							return sendQuestDialog(env, 1352);
						}
					}
					case SETPRO2: {
						return defaultCloseDialog(env, 1, 2, 182215375, 1, 0, 0); // 2
					}
				}
			}
			else if (targetId == 204800) { // Hammel
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 4) {
							return sendQuestDialog(env, 2375);
						}
					}
					case SETPRO5: {
						return defaultCloseDialog(env, 4, 5); // 5
					}
				}
			}
			else if (targetId == 700359 && var == 5 && player.getInventory().getItemCountByItemId(182215377) >= 1) { // Secret Port Entrance
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(), 1757.82f, 1392.94f, 401.75f, (byte) 94);
					return true;
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204707) { // Mani
				if (env.getDialog() == DialogAction.USE_OBJECT) {
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
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				// TODO: readable text dialog
				return HandlerResult.fromBoolean(useQuestItem(env, item, 2, 3, false)); // 3
			}
		}
		return HandlerResult.FAILED;
	}

	@Override
	public boolean onEnterZoneEvent(final QuestEnv env, ZoneName name) {
		Player player = env.getPlayer();
		if (player == null)
			return false;
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (name == ZoneName.get("MINE_PORT_220040000")) {
				if(var == 5) {
					ThreadPoolManager.getInstance().schedule(new Runnable() {
						@Override
						public void run() {
						playQuestMovie(env, 236);
						}
					}, 10000);	
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId != 236)
			return false;
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			changeQuestStep(env, 5, 5, true); // reward
			removeQuestItem(env, 182215377, 1);
			return true;
		}
		return false;
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 24050);
	}
}
