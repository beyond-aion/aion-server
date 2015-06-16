package quest.gelkmaros;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;


/**
 * @author Ritsu
 *
 */
public class _24060MarchtoBalaurea extends QuestHandler {
	
	private final static int questId = 24060;
	
	private final static int[] npc_ids = {204052, 798800, 798409, 799225, 799364 , 799365, 799226};
	
	public _24060MarchtoBalaurea(){
		super(questId);
	}
		@Override
		public void register() {
			for(int npc_id : npc_ids)
				qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
			qe.registerOnEnterZone(ZoneName.get("GELKMAROS_FORTRESS_220070000"), questId);
			
		}
		
		@Override
		public boolean onDialogEvent(QuestEnv env) {
			Player player = env.getPlayer();
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			DialogAction dialog = env.getDialog();
			int targetId = env.getTargetId();

			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);
				if (targetId == 204052) { // Vidar
					if (dialog == DialogAction.QUEST_SELECT) {
						return sendQuestDialog(env, 1011);
					}
					else if (dialog == DialogAction.SETPRO1) {
						return defaultCloseDialog(env, 0, 1);
					}
				}
				if (targetId == 798800 && var == 1) { // Agehia
					 if (dialog == DialogAction.QUEST_SELECT) {
						 return sendQuestDialog(env, 1352);
					 }
					 else if (dialog == DialogAction.SETPRO2) {
						 return defaultCloseDialog(env, 1, 2);
					 }
				 }
				 if (targetId == 798409 && var == 2) { // Tigrina
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 1693);
						}
						else if (dialog == DialogAction.SETPRO3) {
							return defaultCloseDialog(env, 2, 3);
						}
				 }
				 if (targetId == 799225 && var == 3) { // Richelle
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2034);
						}
						else if (dialog == DialogAction.SETPRO4) {
							return defaultCloseDialog(env, 3, 4);
						}
				 }
				 if (targetId == 799364 && var == 4) { // Merhen
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2375);
						}
						else if (dialog == DialogAction.SETPRO5) {
							return defaultCloseDialog(env, 4, 5);
						}
				 }
				 if (targetId == 799365 && var == 5) { // Hogidin
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2375);
						}
						else if (dialog == DialogAction.SETPRO5) {
							return defaultCloseDialog(env, 5, 5, true, false);
						}
				 }
			}
			else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
				if (targetId == 799226) { // Valetta
					if (dialog == DialogAction.USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					}
					return sendQuestEndDialog(env);
				}
			}
			return false;
		}
		
		@Override
		public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
			return defaultOnEnterZoneEvent(env, zoneName, ZoneName.get("GELKMAROS_FORTRESS_220070000"));
		}
}
