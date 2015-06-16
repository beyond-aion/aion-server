package quest.inggison;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;


/**
 * @author Cheatkiller
 *
 */
public class _14060TheLandOfIde extends QuestHandler {
	
	private final static int questId = 14060;
	
	private final static int[] npc_ids = {203700, 798600, 798408, 798926, 799053 , 798927};
	
	public _14060TheLandOfIde(){
		super(questId);
	}
		@Override
		public void register() {
			for(int npc_id : npc_ids)
				qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
			qe.registerOnEnterZone(ZoneName.get("INGGISON_ILLUSION_FORTRESS_210050000"), questId);
			
		}
		
		@Override
		public boolean onDialogEvent(QuestEnv env) {
			Player player = env.getPlayer();
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			DialogAction dialog = env.getDialog();
			int targetId = env.getTargetId();

			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);
				if (targetId == 203700) {
					if (dialog == DialogAction.QUEST_SELECT) {
						return sendQuestDialog(env, 1011);
					}
					else if (dialog == DialogAction.SETPRO1) {
						return defaultCloseDialog(env, 0, 1);
					}
				}
				if (targetId == 798600 && var == 1) {
					 if (dialog == DialogAction.QUEST_SELECT) {
						 return sendQuestDialog(env, 1352);
					 }
					 else if (dialog == DialogAction.SETPRO2) {
						 return defaultCloseDialog(env, 1, 2);
					 }
				 }
				 if (targetId == 798408 && var == 2) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 1693);
						}
						else if (dialog == DialogAction.SETPRO3) {
							return defaultCloseDialog(env, 2, 3);
						}
				 }
				 if (targetId == 798926 && var == 3) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2034);
						}
						else if (dialog == DialogAction.SETPRO4) {
							return defaultCloseDialog(env, 3, 4);
						}
				 }
				 if (targetId == 799053 && var == 4) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2375);
						}
						else if (dialog == DialogAction.SETPRO5) {
							return defaultCloseDialog(env, 4, 4, true, false);
						}
				 }
			}
			else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
				if (targetId == 798927) {
					if (dialog == DialogAction.USE_OBJECT) {
						return sendQuestDialog(env, 2716);
					}
					return sendQuestEndDialog(env);
				}
			}
			return false;
		}
		
		@Override
		public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
			return defaultOnEnterZoneEvent(env, zoneName, ZoneName.get("INGGISON_ILLUSION_FORTRESS_210050000"));
		}
}
