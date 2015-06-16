package quest.gelkmaros;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.SystemMessageId;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Nephis
 * @reworked & modified Gigi
 */
public class _20023KumbandasWhereabouts extends QuestHandler {

	private final static int questId = 20023;
	private final static int[] npc_ids = { 799226, 799292, 700810, 204057, 730243, 799513, 799341, 700706, 799515 };

	public _20023KumbandasWhereabouts() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnDie(questId);
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(216592).addOnKillEvent(questId);
		qe.registerOnMovieEndQuest(442, questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (var == 6) {
				if (player.getWorldId() == 300150000) {
					qs.setQuestVar(7);
					updateQuestStatus(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;
		int var = qs.getQuestVars().getQuestVars();
		if (var == 8) {
			qs.setQuestVar(7);
			updateQuestStatus(env);
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(SystemMessageId.QUEST_FAILED_$1,
				DataManager.QUEST_DATA.getQuestById(questId).getName()));
		}

		return false;
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		int[] quests = { 20020, 2094 };
		return defaultOnLvlUpEvent(env, quests, true);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int instanceId = player.getInstanceId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		int var = qs.getQuestVarById(0);

		if (targetId == 216592) {
			if (var == 8) {
				QuestService.addNewSpawn(300150000, instanceId, 799341, 561.8763f, 192.25128f, 135.88919f, (byte) 30);
				qs.setQuestVarById(0, var + 1);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId != 442)
			return false;
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START || qs.getQuestVars().getQuestVars() != 8)
			return false;
		QuestService.addNewSpawn(300150000, player.getInstanceId(), 216592, (float) 561.8763, (float) 192.25128,
			(float) 135.88919, (byte) 30);
		return true;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int var1 = qs.getQuestVarById(1);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		Npc npc = (Npc) player.getTarget();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799226) {
				if (env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if (env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id())
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
			return false;
		}
		else if (qs.getStatus() != QuestStatus.START)
			return false;
		if (targetId == 799226) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
					else if (var == 3)
						return sendQuestDialog(env, 2034);
				case SETPRO1:
					return defaultCloseDialog(env, 0, 1); // 1
				case SETPRO4:
					if (defaultCloseDialog(env, 3, 4)) { // 4
					 TeleportService2.teleportToNpc(player, 204057);
					 return true;
					}
			}
		}
		else if (targetId == 700810) {
			if (env.getDialog() == DialogAction.USE_OBJECT)
				return true; // loot
		}
		else if (targetId == 799292) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1352);
					else if (var == 2)
						return sendQuestDialog(env, 1693);
					else if (var == 5)
						return sendQuestDialog(env, 2716);
					else if (var == 11)
						return sendQuestDialog(env, 1608);
					break;
				case CHECK_USER_HAS_QUEST_ITEM:
					if (QuestService.collectItemCheck(env, true)) {
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
					else
						return sendQuestDialog(env, 10001);
				case SETPRO2:
					return defaultCloseDialog(env, 1, 2); // 2
				case SETPRO6:
					return defaultCloseDialog(env, 5, 6, 0, 0, 182207611, 1); // 6
				case SET_SUCCEED:
					return defaultCloseDialog(env, 11, 11, true, false); // reward
			}
		}
		else if (targetId == 204057) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 4)
						return sendQuestDialog(env, 2375);
					break;
				case SETPRO5:
					if (defaultCloseDialog(env, 4, 5, 182207611, 1, 0, 0)) { // 5
					 TeleportService2.teleportToNpc(player, 799292);
					 return true;
					}
			}
		}
		else if (targetId == 799341) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 9)
						return sendQuestDialog(env, 4080);
				case SETPRO10:
					return defaultCloseDialog(env, 9, 10, 182207613, 1, 0, 0); // 10
			}
		}
		else if (targetId == 799513 || targetId == 799514 || targetId == 799515 || targetId == 799516) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 7)
						return sendQuestDialog(env, 4080);
				case SETPRO10:
					if (var == 7 && var1 == 3) {
						playQuestMovie(env, 442);
						qs.setQuestVar(8);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						npc.getController().onDie(player);
						return true;
					}
					else if (var == 7 && var1 < 3) {
						qs.setQuestVarById(1, var1 + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						npc.getController().onDie(player);
						return true;
					}
			}
		}
		else if (targetId == 730243) {
			switch (env.getDialog()) {
				case USE_OBJECT:
					if (var >= 6)
						return sendQuestDialog(env, 3057);
					break;
				case QUEST_SELECT:
					if (var == 6)
						return sendQuestDialog(env, 3057);
					break;
				case SETPRO7:
					if (var > 5) {
						WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300150000);
						InstanceService.registerPlayerWithInstance(newInstance, player);
						TeleportService2.teleportTo(player, 300150000, newInstance.getInstanceId(), 561.8651f, 221.91483f,
							134.53333f, (byte) 90);
						return true;
					}
					break;
			}
		}
		else if (targetId == 700706) {
			switch (env.getDialog()) {
				case USE_OBJECT:
					if (var == 10) {
						qs.setQuestVar(11);
						updateQuestStatus(env);
						TeleportService2.teleportTo(player, 220070000, 2274.8582f, 1602.2708f, 412.31882f, (byte) 90);
					}
			}
		}
		return false;
	}
}
