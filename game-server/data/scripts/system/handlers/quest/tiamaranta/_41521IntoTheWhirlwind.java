package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.zone.ZoneName;


/**
 * @author Cheatkiller
 *
 */
public class _41521IntoTheWhirlwind extends QuestHandler {

	private final static int questId = 41521;
	private final static int mobs [] = {218278, 218279, 218284, 218285};

	public _41521IntoTheWhirlwind() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182212525, questId);
		qe.registerQuestNpc(205962).addOnQuestStart(questId);
		qe.registerQuestNpc(205962).addOnTalkEvent(questId);
		qe.registerQuestNpc(701134).addOnTalkEvent(questId);
		qe.registerQuestNpc(205963).addOnTalkEvent(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205962) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else if(dialog == DialogAction.QUEST_ACCEPT_SIMPLE) {
					giveQuestItem(env, 182212525, 1);
					return sendQuestStartDialog(env);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 701134 && player.getEffectController().hasAbnormalEffect(2252)) {
				if (qs.getQuestVarById(0) == 0)
					changeQuestStep(env, 0, 1, false);
				TeleportService2.teleportTo(player, player.getWorldId(), 1097.74f, 124.24f, 61.56f, (byte) 40);
				return true;
			}
			else if (targetId == 205963) {
				switch (dialog) {
					case QUEST_SELECT: {
						if(qs.getQuestVarById(0) == 3)
							return sendQuestDialog(env, 2034);
						else
						return sendQuestDialog(env, 1352);
					}
					case SETPRO2:{
						if(qs.getQuestVarById(0) == 3) {
							removeQuestItem(env, 182212525, 1);
							return defaultCloseDialog(env, 3, 3, true, false);
						}
						else
						return defaultCloseDialog(env, 1, 2);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205962) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 10002);
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
		int targetId = env.getTargetId();
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				checkAndUpdateVarMobs(qs, env, targetId);
			}
		}
		return false;
	}
		
	
	private void checkAndUpdateVarMobs(QuestState qs, QuestEnv env, int targetId){
		int var1 = qs.getQuestVarById(1);
		int var2 = qs.getQuestVarById(2);
		switch(targetId){
			case 218278:
			case 218279:
				if (var1 != 6) {
				qs.setQuestVarById(1, var1 + 1);
				updateQuestStatus(env);
				}
				isAllKilledMobs(qs, env);
				break;
			case 218284:
			case 218285:
				if (var2 != 8) {
				qs.setQuestVarById(2, var2 + 1);
				updateQuestStatus(env);
				}
				isAllKilledMobs(qs, env);
				break;
		}
	}
	
	private void isAllKilledMobs(QuestState qs, QuestEnv env) {
		if(qs.getQuestVarById(1) == 6 && qs.getQuestVarById(2) == 8) {
			qs.setQuestVarById(1, 0);
			qs.setQuestVarById(2, 0);
			changeQuestStep(env, 2, 3, false);
		}
	}
	
	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if ((var == 0 || var == 3) && player.isInsideZone(ZoneName.get("LDF4B_ITEMUSEAREA_Q41521A")))
				SkillEngine.getInstance().applyEffectDirectly(2252, player, player, (60 * 1000));
				return HandlerResult.SUCCESS;
		}
		return HandlerResult.FAILED;
	}
}
