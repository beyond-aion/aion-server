package quest.crafting;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.craft.CraftSkillUpdateService;

/**
 * @author Ritsu
 * @modified Pad
 */
public class _19053ExpertExpertofConstruction extends QuestHandler {

	private final static int questId = 19053;

	public _19053ExpertExpertofConstruction() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798450).addOnQuestStart(questId);
		qe.registerQuestNpc(798450).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (dialog == DialogAction.QUEST_SELECT && !CraftSkillUpdateService.getInstance().canLearnMoreExpertCraftingSkill(player)) {
			return sendQuestSelectionDialog(env);
		}

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 798450) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 798450:
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 2375);
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							if (QuestService.collectItemCheck(env, true)) {
								changeQuestStep(env, 0, 0, true);
								return sendQuestDialog(env, 5);
							} else
								return sendQuestDialog(env, 2716);
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798450) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
