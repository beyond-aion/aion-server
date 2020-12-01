package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * @author Artur, Majka, Sykra
 */
public class _14025CookingUpDisasters extends AbstractQuestHandler {

	private final static int[] mobs = { 211017, 217090, 232133 };

	public _14025CookingUpDisasters() {
		super(14025);
	}

	@Override
	public void register() {
		int[] npcs = { 203989, 203901, 204020 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerOnDie(questId);
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		for (int mob : mobs)
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 14020);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 14020);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		switch (targetId) {
			case 211017:
			case 232133:
				int killVar1 = qs.getQuestVarById(1);
				if (var == 5 && killVar1 < 4) {
					qs.setQuestVarById(1, killVar1 + 1);
					updateQuestStatus(env);
					return true;
				}
				break;
			case 217090:
				int killVar2 = qs.getQuestVarById(2);
				if (var == 5 && killVar2 < 1) {
					qs.setQuestVarById(2, killVar2 + 1);
					updateQuestStatus(env);
					return true;
				}
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203901) // Telemachus
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 3057);
				}
			return sendQuestEndDialog(env);
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203989) { // Tumblusen
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
						else if (var == 1)
							return sendQuestDialog(env, 1352);
						else if (var == 4)
							return sendQuestDialog(env, 2034);
						else if (var == 5)
							return sendQuestDialog(env, 2716);
						break;
					case SELECT1_1_1:
						if (var == 0)
							playQuestMovie(env, 183);
						break;
					case SETPRO1:
						if (var == 0)
							return defaultCloseDialog(env, var, var + 1); // 1
						else
							return sendQuestDialog(env, 1352);
					case CHECK_USER_HAS_QUEST_ITEM:
						if (var == 1) {
							if (QuestService.collectItemCheck(env, true)) {
								changeQuestStep(env, var, var + 1, false); // 2
								return sendQuestDialog(env, 1438);
							} else
								return sendQuestDialog(env, 1353);
						}
						return false;
					case SETPRO2:
						if (var == 2)
							return defaultCloseDialog(env, var, var + 1); // 3
						return false;
					case SETPRO4:
						if (var == 4)
							return defaultCloseDialog(env, var, var + 1); // 5
						break;
					case SETPRO6:
						if (var == 5) {
							changeQuestStep(env, 5, 6, true);
							return closeDialogWindow(env);
						}
				}
			} else if (targetId == 204020) { // Mabangtah
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 3)
							return sendQuestDialog(env, 1693);
						if (var == 6)
							return sendQuestDialog(env, 2034);
						else if (var == 10)
							return sendQuestDialog(env, 3057);
						break;
					case SETPRO3:
						if (var == 3) {
							TeleportService.teleportTo(player, 210020000, 1761.0742f, 907.1806f, 427.8147f, (byte) 47, TeleportAnimation.FADE_OUT_BEAM);
							qs.setQuestVar(4);
							updateQuestStatus(env);
							return true;
						}
						break;
					case SETPRO7:
						return defaultCloseDialog(env, 10, 10, true, false); // reward
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			if (var >= 7 && var <= 10) {
				qs.setQuestVarById(0, 6); // 6
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}
}
