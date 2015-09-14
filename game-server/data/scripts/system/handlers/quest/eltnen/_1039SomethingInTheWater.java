package quest.eltnen;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Xitanium
 * @reworked vlog
 */
public class _1039SomethingInTheWater extends QuestHandler {

	private final static int questId = 1039;

	public _1039SomethingInTheWater() {
		super(questId);
	}

	@Override
	public void register() {
		int[] mobs = { 210946, 210968, 210969, 210947 };
		qe.registerQuestItem(182201009, questId);
		qe.registerQuestNpc(203946).addOnTalkEvent(questId);
		qe.registerQuestNpc(203705).addOnTalkEvent(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		for (int mob : mobs)
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			int var1 = qs.getQuestVarById(1);
			int var2 = qs.getQuestVarById(2);
			switch (targetId) {
				case 203946: { // Asclepius
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							} else if (var == 3) {
								return sendQuestDialog(env, 1693);
							} else if (var == 4 && var1 == 3 && var2 == 3) {
								return sendQuestDialog(env, 2034);
							}
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1, 182201009, 1, 0, 0); // 1
						}
						case SETPRO3: {
							return defaultCloseDialog(env, 3, 4); // 4
						}
						case SELECT_QUEST_REWARD: {
							qs.setStatus(QuestStatus.REWARD); // reward
							updateQuestStatus(env);
							return sendQuestDialog(env, 5);
						}
					}
					break;
				}
				case 203705: { // Jumentis
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 1352);
							}
						}
						case SETPRO2: {
							defaultCloseDialog(env, 2, 3, 0, 0, 182201010, 1); // 3
							TeleportService2.teleportTo(player, 210020000, 1910.61f, 2020.29f, 361.48f, (byte) 85, TeleportAnimation.BEAM_ANIMATION);
							return true;
						}
					}
					break;
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203946) { // Asclepius
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		if (player.isInsideZone(ZoneName.get("LF2_ITEMUSEAREA_Q1039"))) {
			return HandlerResult.fromBoolean(useQuestItem(env, item, 1, 2, false, 182201010, 1, 0)); // 2
		}
		return HandlerResult.FAILED;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 4) {
				int[] vaegir = { 210946, 210968 };
				int[] fighter = { 210969, 210947 };
				switch (targetId) {
					case 210946:
					case 210968: {
						return defaultOnKillEvent(env, vaegir, 0, 3, 1); // 1: 3
					}
					case 210969:
					case 210947: {
						return defaultOnKillEvent(env, fighter, 0, 3, 2); // 2: 3
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 1300, true);
	}

}
