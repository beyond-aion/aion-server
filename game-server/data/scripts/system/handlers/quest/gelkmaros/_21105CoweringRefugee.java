package quest.gelkmaros;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;


/**
 * @author Cheatkiller
 *
 */
public class _21105CoweringRefugee extends QuestHandler {

	private final static int questId = 21105;

	public _21105CoweringRefugee() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799276).addOnQuestStart(questId);
		qe.registerQuestNpc(799276).addOnTalkEvent(questId);
		qe.registerQuestNpc(700812).addOnTalkEvent(questId);
		qe.registerQuestNpc(799366).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 799276) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else {
					return sendQuestStartDialog(env, 182207857, 1);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799366) {
				if (dialog == DialogAction.QUEST_SELECT) {
						return sendQuestDialog(env, 1011);
				}
				else if (dialog == DialogAction.SET_SUCCEED) {
					Npc npc = (Npc) env.getVisibleObject();
					npc.getController().onDelete();
					removeQuestItem(env, 182207857, 1);
					return defaultCloseDialog(env, 0, 1, true, false);
				}
			}
			else if (targetId == 700812) {
				Npc npc = (Npc) env.getVisibleObject();
				int npcId [] = {799366, 216086};
				QuestService.addNewSpawn(npc.getWorldId(), npc.getInstanceId(), npcId[Rnd.get(0, npcId.length -1)], npc.getX(), npc.getY(), npc.getZ(), (byte) 0);
				npc.getController().scheduleRespawn();
				npc.getController().onDelete();
				return true;
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799276) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
