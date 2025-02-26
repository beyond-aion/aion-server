package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @Author Majka
 */
public class _10031ARiskfortheObelisk extends AbstractQuestHandler {

	private final static int[] mobs = { 215504, 215505, 215516, 215517, 215518, 215519, 216463, 216464, 216647, 216648, 216691, 216692, 216782, 216783,
		215508, 215509 };

	public _10031ARiskfortheObelisk() {
		super(10031);
	}

	@Override
	public void register() {
		// Fasimedes ID: 203700
		// Southern Obelisk Support ID: 702662
		// Overheated Obelisk ID: 730224
		// Sibylle ID: 798408
		// Eremitia ID: 798600
		// Outremus ID: 798926
		// Versetti ID: 798927
		// Steropes ID: 799052
		int[] npcs = { 203700, 702662, 730224, 798408, 798600, 798926, 798927, 799052 };
		qe.registerOnLevelChanged(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerOnEnterWorld(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203700) { // Fasimedes
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						}
						return false;
					case SETPRO1:
						return defaultCloseDialog(env, var, var + 1); // 1
				}
			} else if (targetId == 798600) { // Eremitia
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 1) {
							return sendQuestDialog(env, 1352);
						}
						break;
					case SETPRO2:
						return defaultCloseDialog(env, var, var + 1); // 2
				}
			} else if (targetId == 798408) { // Sibylle
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 2) {
							return sendQuestDialog(env, 1693);
						}
						break;
					case SETPRO3:
						qs.setQuestVar(var + 1); // 3
						updateQuestStatus(env);
						TeleportService.teleportTo(player, 210050000, player.getInstanceId(), 1440, 408, 553, (byte) 77, TeleportAnimation.FADE_OUT_BEAM);
						return closeDialogWindow(env); // 1
				}
			} else if (targetId == 798926) { // Outremus
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 4) {
							return sendQuestDialog(env, 2375);
						}
						break;
					case SETPRO5:
						return defaultCloseDialog(env, var, var + 1); // 5
				}
			} else if (targetId == 799052) { // Steropes
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 5) {
							return sendQuestDialog(env, 2716);
						}
						break;
					case SELECT6_1:
						playQuestMovie(env, 30, true);
						return sendQuestDialog(env, 2717);
					case SETPRO6:
						return defaultCloseDialog(env, var, var + 1); // 6
				}
			} else if (targetId == 798927) { // Versetti
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 6) {
							return sendQuestDialog(env, 3057);
						}
						break;
					case SELECT7_1:
						playQuestMovie(env, 516);
						return sendQuestDialog(env, 3058);
					case SETPRO7:
						giveQuestItem(env, 182215616, 1); // Aether Wave Stone
						giveQuestItem(env, 182215617, 1); // Obelisk
						return defaultCloseDialog(env, var, var + 1); // 7
				}
			} else if (targetId == 730224) { // Overheated Obelisk
				switch (dialogActionId) {
					case USE_OBJECT:
						if (var == 7) {
							removeQuestItem(env, 182215616, 1); // Aether Wave Stone
							return defaultCloseDialog(env, var, var + 1); // 8
						}
						break;
				}
			} else if (targetId == 702662) { // Southern Obelisk Support
				switch (dialogActionId) {
					case USE_OBJECT:
						if (var == 8) {
							removeQuestItem(env, 182215617, 1); // Obelisk
							spawnTemporarily(700600, player.getWorldMapInstance(), 2192, 368, 431, (byte) 90, 1); // Enhanced Obelisk
							qs.setQuestVar(9); // 9
							updateQuestStatus(env);
							return true;
						}
						break;
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798927) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 9) {
				int[] basrasas = { 215504, 215505, 215516, 215517, 215518, 215519, 216463, 216464, 216647, 216648, 216691, 216692, 216782, 216783 };
				int[] spallers = { 215508, 215509 };

				if (defaultOnKillEvent(env, basrasas, 0, 10, 1) || defaultOnKillEvent(env, spallers, 0, 2, 2)) {
					int var1 = qs.getQuestVarById(1);
					int var2 = qs.getQuestVarById(2);
					if (var1 == 10 && var2 == 2) {
						qs.setStatus(QuestStatus.REWARD);
						qs.setQuestVar(10);
						updateQuestStatus(env);
					}
					return true;
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
			if (var == 9) {
				if (player.isInsideZone(ZoneName.get("DF4_ITEMUSEAREA_Q20031_220070000"))) {
					playQuestMovie(env, 566);
					return HandlerResult.fromBoolean(useQuestItem(env, item, 9, 10, false)); // 10
				}
			}
		}
		return HandlerResult.FAILED;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (player.getWorldId() == 210050000) {
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);
				if (var == 3) {
					playQuestMovie(env, 501);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId == 501)
			changeQuestStep(env, 3, 4);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player);
	}
}
