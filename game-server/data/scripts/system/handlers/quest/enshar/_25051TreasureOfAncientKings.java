package quest.enshar;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @Author Majka
 */
public class _25051TreasureOfAncientKings extends QuestHandler {

	private final static int questId = 25051;

	public _25051TreasureOfAncientKings() {
		super(questId);
	}

	@Override
	public void register() {
		// Recluse's grave 731553
		// Recluse's relic 731554
		// Ancient King's treasure chest 731555
		// Soglo 804915
		// Rolef 804916
		// Zagmus 805160
		qe.registerQuestNpc(804915).addOnQuestStart(questId);
		int[] npcs = { 731553, 731554, 731555, 804915, 804916, 805160 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerOnInvisibleTimerEnd(questId);
		qe.registerOnLogOut(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 804915) { // Soglo
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			
			switch(targetId) {
				case 731553: // Recluse's grave
					if(var == 0) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1011);

						if (dialog == DialogAction.SETPRO1) {
							// Spawn of Zagmus
							if(!qe.getQuestNpc(805160).isWasSpawned())
								QuestService.addNewSpawn(220080000, player.getInstanceId(), 805160, 2046.8f, 1588.8f, 348.4f, (byte) 90, 5);
							QuestService.invisibleTimerStart(env, 300);
							return defaultCloseDialog(env, var, var+1);
						}
					}
					break;
				case 805160: // Zagmus
					if(var == 1) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1352);

						if (dialog == DialogAction.SETPRO2)
							return defaultCloseDialog(env, var, var+1);
					}
					break;
				case 731554: // Recluse's relic
					if(var == 2) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1693);

						if (dialog == DialogAction.SETPRO3)
							return defaultCloseDialog(env, var, var+1, 182215720, 1);
					}
					break;
				case 731555: // Ancient King's treasure chest
					if(var == 3) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 2034);

						if (dialog == DialogAction.SET_SUCCEED) {
							Npc npc = (Npc) env.getVisibleObject();
							if(npc != null)
								QuestService.addNewSpawn(220080000, player.getInstanceId(), 220031, npc.getPosition().getX() - 2, npc.getPosition().getY() + 2, npc.getPosition().getZ(), (byte) 10, 5);
							removeQuestItem(env, 182215720, 1);
							qs.setQuestVar(var+1);
							return defaultCloseDialog(env, var+1, var+1, true, false);
						}
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch(targetId) {
				case 804916: // Rolef
					if (dialog == DialogAction.USE_OBJECT)
						return sendQuestDialog(env, 10002);
					
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
	
	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		return RestoreQuestStep(env);
	}
	
	@Override
	public boolean onInvisibleTimerEndEvent(QuestEnv env) {
		return RestoreQuestStep(env);
	}
	
	private boolean RestoreQuestStep(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		
		if(qs == null)
			return false;
		
		int var = qs.getQuestVarById(0);
		if(var == 1) {
			qs.setQuestVar(var-1);
			updateQuestStatus(env);
		}
		return true;
	}
}