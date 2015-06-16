package quest.sarpan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Cheatkiller
 *
 */
 
public class _41172ADaevaInShulackClothing extends QuestHandler {

	private final static int	questId	= 41172;

	public _41172ADaevaInShulackClothing() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205778).addOnQuestStart(questId);
		qe.registerQuestNpc(205778).addOnTalkEvent(questId);
		qe.registerQuestNpc(701160).addOnTalkEvent(questId);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205778) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else if(dialog == DialogAction.QUEST_ACCEPT_SIMPLE) {
					SkillEngine.getInstance().applyEffectDirectly(20413, player, player, 0);
					return sendQuestStartDialog(env);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 701160) {
				changeQuestStep(env, 0, 1, true);
				return true;
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205778) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
