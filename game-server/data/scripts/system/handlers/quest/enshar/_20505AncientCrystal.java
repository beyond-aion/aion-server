package quest.enshar;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @Author Majka
 * @Description:
 * Talk with Cenute.
 * Talk with Malite.
 * Investigate the Field Wardens Bodyguard Corpse nearby.
 * Investigate the Field Warden Corpse.
 * Track down and kill the Beritra Research Corps Warmage in Timeswept Altar.
 * Talk with Cenute.
 * 
 * Order: See Cenute and carry out his mission.
 */
public class _20505AncientCrystal extends QuestHandler {

	private final static int questId = 20505;

	public _20505AncientCrystal() {
		super(questId);
	}

	@Override
	public void register() {
		// Cenute 804732
		// Malite 804733
		int[] npcs = { 804732, 804733, 804734, 804735 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestNpc(219953).addOnKillEvent(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		switch(targetId) {
			case 804732:
				if (qs.getStatus() == QuestStatus.START) {
					if(var == 0) { // Step 0: Talk with Cenute.
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1011);
						
						if (dialog == DialogAction.SETPRO1)
							return defaultCloseDialog(env, var, var+1);
					}
				}
				
				if (qs.getStatus() == QuestStatus.REWARD) {
					if (dialog == DialogAction.USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					}
					
					return sendQuestEndDialog(env);
				}
				break;
			case 804733:
				if (qs.getStatus() == QuestStatus.START) {
					if(var == 1) { // Step 1: Talk with Malite.
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1352);
						
						if (dialog == DialogAction.SETPRO2)
							return defaultCloseDialog(env, var, var+1);
					}
				}
				break;
			case 804734:
				if (qs.getStatus() == QuestStatus.START) {
					if(var == 2) { // Step 2: Investigate the Field Wardens Bodyguard Corpse nearby.
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1693);
						
						if (dialog == DialogAction.SETPRO3)
							return defaultCloseDialog(env, var, var+1);
					}
				}
				break;
			case 804735:
				if (qs.getStatus() == QuestStatus.START) {
					if(var == 3) { // Step 3: Investigate the Field Warden Corpse.
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 2034);
						
						if (dialog == DialogAction.SETPRO4)
							return defaultCloseDialog(env, var, var+1);
					}
				}
				break;
		}
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
	
		switch(targetId) {
			case 219953:
				if (var == 4) { // Step 4: Track down and kill the Beritra Research Corps Warmage in Timeswept Altar.
					qs.setStatus(QuestStatus.REWARD);
					qs.setQuestVar(var+1);
					updateQuestStatus(env);
					return true;
				}
				break;
		}
		return false;
	}
	
	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 20500);
	}
}