package quest.tiamaranta_eye;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 * @author Artur
 */

public class _14091IdentityBusiness extends QuestHandler {
	
	private final static int questId = 14091;
	private final static int[] npc_ids = { 802178, 802063, 798926, 798600, 203700, 205987, 800165 };
	
	public _14091IdentityBusiness() {
		super(questId);
	}
	
	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int targetId = env.getTargetId();
		Npc npc = (Npc) env.getVisibleObject();
		int var = qs.getQuestVarById(0);
		DialogAction dialog = env.getDialog();
		if (qs.getStatus() == QuestStatus.START) {			
			switch (targetId) {
				case 802178:// Oriata of the Past  
					switch (dialog) {
						case USE_OBJECT: {
							return sendQuestDialog(env, 1011);
						}
						case SETPRO1:
							if (targetId == 802178) 
								NpcActions.delete(npc);
							QuestService.addNewSpawn(300500000, player.getInstanceId(), 802063, player.getX(), player.getY(), player.getZ(), (byte) 95);
							qs.setQuestVar(1);
							updateQuestStatus(env);
						break;
					}
				case 802063://Kaisinel
					switch(dialog){
					case USE_OBJECT:{
						return sendQuestDialog(env, 1352);
					}
					case SETPRO2:{
						if (targetId == 802063) 
							NpcActions.delete(npc);
						TeleportService2.teleportTo(player, 210050000, player.getInstanceId(), 1273, 196, 597, (byte) 10);//Outremus
						qs.setQuestVar(2);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
					}
					break;
				case 798926://Outremus 
					switch(dialog){
					case QUEST_SELECT:{
						return sendQuestDialog(env, 1693);
					}
					case SETPRO3:{
						qs.setQuestVar(3);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
					}
					break;
				case 798600://Eremitia 
					switch(dialog){
					case QUEST_SELECT:{
						if(var == 3){
						return sendQuestDialog(env, 2034);
						}
						if(var == 5){
							return sendQuestDialog(env, 2716);
						}
					}
					case SETPRO4:{
						qs.setQuestVar(4);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
					case SETPRO6:{
						qs.setQuestVar(6);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
					}
					break;
				case 203700://Fasimedes 
					switch(dialog){
					case QUEST_SELECT:{
						return sendQuestDialog(env, 2375);
					}
					case SETPRO5:{
						qs.setQuestVar(5);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
					}
					break;
				case 205987://Garnon 
					switch(dialog){
					case QUEST_SELECT:{
						return sendQuestDialog(env, 3057);
					}
					case SETPRO7:{
						qs.setStatus(QuestStatus.REWARD);
						qs.setQuestVar(7);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
					}
					break;
			}
		}
		if(qs != null && qs.getStatus() == QuestStatus.REWARD){
			if(targetId == 800165){//Crispin 
				switch(dialog){
				case QUEST_SELECT:{
					return sendQuestDialog(env, 3398);
				}
				case SELECT_QUEST_REWARD:{
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
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 14090);
	}

}
