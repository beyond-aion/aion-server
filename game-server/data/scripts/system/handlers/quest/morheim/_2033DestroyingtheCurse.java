package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * Talk with Urakon (204391).<br>
 * Talk with Kellan (790020).<br>
 * Gather Spiner Tails and take them to Kellan.<br>
 * Listen to the story of Kellan.<br>
 * Meet Kimssi (204393).<br>
 * Choose a Skurv to guide you.<br>
 * Throw the Cursed Necklace (182204007) into the Boiling Lava (TARANS_CAVERN_220020000).<br>
 * Report back to Kellan.
 * 
 * @author Erin
 * @reworked vlog
 * @Modified Majka
 */
public class _2033DestroyingtheCurse extends QuestHandler {

	private final static int questId = 2033;

	public _2033DestroyingtheCurse() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 204391, 790020, 204393, 204394, 204395, 204396, 204397, 204398 };
		qe.registerOnLevelUp(questId);
		qe.registerQuestItem(182204007, questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204391: { // Urakon
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
					}
					break;
				}
				case 790020: { // Kellan
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							else if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
							else if (var == 3) {
								return sendQuestDialog(env, 10000);
							}
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2); // 2
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							return checkQuestItems(env, 2, 3, false, 10000, 10001); // 3
						}
						case SETPRO4: {
							if (!player.getInventory().isFullSpecialCube()) {
								return defaultCloseDialog(env, 3, 4, 182204007, 1, 0, 0); // 4
							}
							else {
								return defaultCloseDialog(env, 3, 3);
							}
						}
						case FINISH_DIALOG: {
							return defaultCloseDialog(env, 2, 2);
						}
					}
					break;
				}
				case 204393: { // Kimssi
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 4) {
								return sendQuestDialog(env, 2375);
							}
						}
						case SELECT_ACTION_2376: {
							if (var == 4) {
								playQuestMovie(env, 74);
								return sendQuestDialog(env, 2376);
							}
						}
						case SETPRO5: {
							return defaultCloseDialog(env, 4, 5); // 5
						}
					}
					break;
				}
				case 204394:
				case 204395:
				case 204396:
				case 204397:
				case 204398: {
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 5) {
								return sendQuestDialog(env, 2716);
							}
						}
						case SETPRO6: {
							return defaultCloseDialog(env, 5, 6); // 6
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 790020) { // Kellan
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 6) {
				if (player.isInsideItemUseZone(ZoneName.get("DF2_ITEMUSEAREA_Q2033"))) {
					return HandlerResult.fromBoolean(useQuestItem(env, item, 6, 6, true, 75)); // reward
				}
			}
		}
		return HandlerResult.SUCCESS; // ??
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 2300, true); // Sets as zone mission to avoid it appears on new player list.
	}
}
