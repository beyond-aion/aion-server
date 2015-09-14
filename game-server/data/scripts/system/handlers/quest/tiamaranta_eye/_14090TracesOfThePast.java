package quest.tiamaranta_eye;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Artur
 * 
 */

public class _14090TracesOfThePast extends QuestHandler {
	
	private final static int questId = 14090;
	private final static int[] npc_ids = { 802059, 730889, 802178, 205988, 730890 };
	
	public _14090TracesOfThePast() {
		super(questId);
	}
	
	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerQuestItem(182215408, questId);
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
				case 802059:// Protector Oriata  
					switch (dialog) {
						case QUEST_SELECT: {
							if(var == 0){
							return sendQuestDialog(env, 1011);
							}
						}
						case SETPRO1:{
							if (!giveQuestItem(env, 182215408, 1))
								return true;
							qs.setQuestVar(1);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
					}
					break;
				case 730889://Tiamat's Remains
					switch(dialog){
					case QUEST_SELECT:{
						if(var == 2)
						return sendQuestDialog(env, 1693);
					}
					case SETPRO3:{
						if (targetId == 730889) 
							NpcActions.delete(npc);
						QuestService.addNewSpawn(300490000, player.getInstanceId(), 802178, (float) 461.54, (float) 514.5, 417, (byte) 119);
						qs.setQuestVar(3);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}						
					}
					break;
				case 802178://Oriata of the Past
					switch(dialog){
					case USE_OBJECT:{
						if(var == 3){
							return sendQuestDialog(env, 2034);
						}						
					}
					case SETPRO4:{
						if (targetId == 802178) 
							NpcActions.delete(npc);
						TeleportService2.teleportTo(player, 300500000, player.getInstanceId(), 247, 239, 124, (byte) 10);
						QuestService.addNewSpawn(300500000, player.getInstanceId(), 205988, (float) 247.54, (float) 239.5, 124, (byte) 95);
						qs.setQuestVar(4);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
					
					}
					break;
				case 205988: //Israphel
					switch(dialog){
					case USE_OBJECT:{
						if(var == 4)
						return sendQuestDialog(env, 2375);
					}
					case SETPRO5:{
						if (targetId == 205988) 
							NpcActions.delete(npc);
						QuestService.addNewSpawn(300500000, player.getInstanceId(), 730890, player.getX(), player.getY(), player.getZ(), (byte) 95);
						qs.setQuestVar(5);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
					}
					break;
				case 730890://Concentrated Ide Crystal
					switch(dialog){
					case QUEST_SELECT:{
						if(var == 5)
						return sendQuestDialog(env, 2716);
					}
					case SETPRO6:{						
						if (targetId == 730890) 
							NpcActions.delete(npc);
						qs.setQuestVar(6);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						QuestService.addNewSpawn(300500000, player.getInstanceId(), 802178, player.getX(), player.getY(), player.getZ(), (byte) 95);
						return closeDialogWindow(env);
					}
					}
			}
		}
		if(qs != null && qs.getStatus() == QuestStatus.REWARD){
			if(targetId == 802178){//Oriata of the Past
				switch(dialog){
				case USE_OBJECT:{
					if(var == 6){
						return sendQuestDialog(env, 3057);
					}
				}
				case SELECT_QUEST_REWARD:{
					
					return sendQuestDialog(env, 5);
				}
				case SELECTED_QUEST_REWARD1:{					
					return sendQuestEndDialog(env);
				}				
				case SELECTED_QUEST_REWARD2:{
					return sendQuestEndDialog(env);
				}			
				}
								
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
			if (player.isInsideZone(ZoneName.get("LDF4B_ITEMUSEAREA_Q14090"))) {				
				if (var == 1) {
					TeleportService2.teleportTo(player, 300490000, player.getInstanceId(), 504, 515, 417, (byte) 10);
					QuestService.addNewSpawn(300490000, player.getInstanceId(), 730889, (float) 504.54, (float) 515.5, 417, (byte) 95);
					return HandlerResult.fromBoolean(useQuestItem(env, item, 1, 2, false));
				}
			}
		}
		return HandlerResult.SUCCESS;
	}
	
	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 14081);
	}

}
