package quest.inggison;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Nephis
 * @reworked & modified Gigi, Luzien
 */
public class _10022SupportTheInggisonOutpost extends QuestHandler {

   private final static int questId = 10022;
   private final static int[] npc_ids = {798932, 798996, 203786, 204656, 798176, 798926, 700601};

   public _10022SupportTheInggisonOutpost() {
	  super(questId);
   }

   @Override
   public void register() {
	  qe.registerOnEnterZoneMissionEnd(questId);
	  qe.registerOnLevelUp(questId);
	  qe.registerQuestNpc(215622).addOnKillEvent(questId);
	  qe.registerQuestNpc(216784).addOnKillEvent(questId);
	  qe.registerQuestNpc(215633).addOnKillEvent(questId);
	  qe.registerQuestNpc(216731).addOnKillEvent(questId);
	  qe.registerQuestNpc(215634).addOnKillEvent(questId);
	  for (int npc_id : npc_ids) {
		 qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	  }
   }

   @Override
   public boolean onLvlUpEvent(QuestEnv env) {
	  return defaultOnLvlUpEvent(env, 10020, true);
   }

   @Override
   public boolean onZoneMissionEndEvent(QuestEnv env) {
	  return defaultOnZoneMissionEndEvent(env);
   }

   @Override
   public boolean onDialogEvent(final QuestEnv env) {
	  final Player player = env.getPlayer();
	  final QuestState qs = player.getQuestStateList().getQuestState(questId);
	  if (qs == null)
		 return false;

	  int var = qs.getQuestVarById(0);
	  int var3 = qs.getQuestVarById(3);
	  int targetId = 0;
	  if (env.getVisibleObject() instanceof Npc)
		 targetId = ((Npc) env.getVisibleObject()).getNpcId();

	  if (qs.getStatus() == QuestStatus.REWARD) {
		 if (targetId == 798926) {
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
	  if (targetId == 798932) {
		 switch (env.getDialog()) {
			case QUEST_SELECT:
			   if (var == 0)
				  return sendQuestDialog(env, 1011);
			   else if (var == 11)
				  return sendQuestDialog(env, 1608);
			case SETPRO1:
			   return defaultCloseDialog(env, 0, 1); // 1
			case SET_SUCCEED:
			   return defaultCloseDialog(env, 11, 11, true, false); // reward
			}
	  }
	  else if (targetId == 798996) {
		 switch (env.getDialog()) {
			case QUEST_SELECT:
			   if (var == 1)
				  return sendQuestDialog(env, 1352);
			   else if (var == 3)
				  return sendQuestDialog(env, 2034);
			   else if (var == 10)
				  return sendQuestDialog(env, 4080);
			case SETPRO2:
			   return defaultCloseDialog(env, 1, 2); // 2
			case SETPRO4:
			   changeQuestStep(env, 3, 4, false);
			   PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
			   return true;
			case SETPRO10:
			   if (var == 10)
				  return defaultCloseDialog(env, 10, 11); // 11
			}
	  }
	  else if (targetId == 203786) {
		 switch (env.getDialog()) {
			case QUEST_SELECT:
			   if (var == 4)
				  return sendQuestDialog(env, 2375);
			   else if (var == 7)
				  return sendQuestDialog(env, 3398);
			   else if (var == 8)
				  return sendQuestDialog(env, 3739);
			case CHECK_USER_HAS_QUEST_ITEM:
			   if (QuestService.collectItemCheck(env, true)) {
				  qs.setQuestVarById(0, var + 1);
				  updateQuestStatus(env);
				  PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
				  return true;
			   }
			   else
				  return sendQuestDialog(env, 10001);
			case SETPRO8:
			   if (var == 7)
				  return defaultCloseDialog(env, 7, 8); // 8
			   break;
			case SETPRO9:
			   if (var == 8)
				  return defaultCloseDialog(env, 8, 9); // 9
			}
	  }
	  else if (targetId == 204656) {
		 switch (env.getDialog()) {
			case QUEST_SELECT:
			   if (var == 5)
				  return sendQuestDialog(env, 2716);
			case SETPRO6:
			   if (var == 5)
				  return defaultCloseDialog(env, 5, 6); // 6
			}
	  }
	  else if (targetId == 798176) {
		 switch (env.getDialog()) {
			case QUEST_SELECT:
			   if (var == 6)
				  return sendQuestDialog(env, 3057);
			case SETPRO7:
			   if (var == 6)
				  return defaultCloseDialog(env, 6, 7); // 7
			}
	  }
	  else if (targetId == 700601) {
		 if (var == 9 && env.getDialog() == DialogAction.USE_OBJECT) {
			if (var3 < 4)
			   return useQuestObject(env, var3, var3 + 1, false, 3, true); // 3: 1-4
			else if (var3 == 4) {
			   useQuestObject(env, 4, 5, false, 3, true); // 3: 5
			   qs.setQuestVar(10);
			   updateQuestStatus(env);
			}
		 }
	  }
	  return false;
   }

   @Override
   public boolean onKillEvent(QuestEnv env) {
	  Player player = env.getPlayer();
	  QuestState qs = player.getQuestStateList().getQuestState(questId);
	  if (qs == null || qs.getStatus() != QuestStatus.START || qs.getQuestVarById(0) != 2)
		 return false;

	  int[] first = {215622, 216784};
	  int[] second = {215633, 216731, 215634};

	  if (defaultOnKillEvent(env, first, 0, 10, 1) || defaultOnKillEvent(env, second, 0, 10, 2)) {
		 int var1 = qs.getQuestVarById(1);
		 int var2 = qs.getQuestVarById(2);
		 if (var1 == 10 && var2 == 10) {
			qs.setQuestVar(3);
			updateQuestStatus(env);
		 }
		 return true;
	  }

	  return false;
   }
}
