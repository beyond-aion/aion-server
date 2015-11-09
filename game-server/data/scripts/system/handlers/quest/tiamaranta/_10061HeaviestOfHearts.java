package quest.tiamaranta;

import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Luzien
 * @modified Enomine
 */
public class _10061HeaviestOfHearts extends QuestHandler {

	private final static int questId = 10061;
	final List<Npc> balaur = new FastTable<Npc>();

	private final static int[] mobs = { 218826, 218827, 218828 };

	public _10061HeaviestOfHearts() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerOnQuestTimerEnd(questId);
		qe.registerOnDie(questId);
		qe.registerQuestNpc(205886).addOnTalkEvent(questId);
		qe.registerQuestNpc(800019).addOnTalkEvent(questId);
		qe.registerOnMovieEndQuest(751, questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 10060);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205886) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						}
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1);
					}
				}
			} else if (targetId == 800019) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 2) {
							return sendQuestDialog(env, 1693);
						}
					}
					case SETPRO3: {
						playQuestMovie(env, 751);
						return defaultCloseDialog(env, 2, 3);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800019) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 1) {
				checkAndUpdateVar(qs, env, targetId);
			}
		}
		return false;
	}

	private void checkAndUpdateVar(QuestState qs, QuestEnv env, int targetId) {
		switch (targetId) {
			case 218826:
				qs.setQuestVarById(1, 1);
				updateQuestStatus(env);
				isAllKilledMobs(qs, env);
				break;
			case 218827:
				qs.setQuestVarById(2, 1);
				updateQuestStatus(env);
				isAllKilledMobs(qs, env);
				break;
			case 218828:
				qs.setQuestVarById(3, 1);
				updateQuestStatus(env);
				isAllKilledMobs(qs, env);
				break;
		}
	}

	private void isAllKilledMobs(QuestState qs, QuestEnv env) {
		if (qs.getQuestVarById(1) == 1 && qs.getQuestVarById(2) == 1 && qs.getQuestVarById(3) == 1) {
			qs.setQuestVarById(1, 0);
			qs.setQuestVarById(2, 0);
			qs.setQuestVarById(3, 0);
			changeQuestStep(env, 1, 2, false);
		}
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			for (Npc npc : balaur) {
				npc.getController().onDelete();
			}
			int var = qs.getQuestVarById(0);
			if (var == 3) {
				qs.setQuestVar(2);
				updateQuestStatus(env);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_SYSTEMMSG_GIVEUP(DataManager.QUEST_DATA.getQuestById(questId)
					.getName()));
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		Player player = env.getPlayer();
		Npc npc = (Npc) player.getTarget();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (movieId == 751 && var == 3) {
				spawnAndAttack(player, npc);
				QuestService.questTimerStart(env, 30);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 3) {
				changeQuestStep(env, 3, 4, true);
				return true;
			}
		}
		return false;
	}

	private void spawnAndAttack(Player player, Npc npc) {
		balaur.add((Npc) QuestService.spawnQuestNpc(600030000, player.getInstanceId(), 218825, 2454.169f, 165.504f, 324.518f, (byte) 9));
		balaur.add((Npc) QuestService.spawnQuestNpc(600030000, player.getInstanceId(), 218765, 2453.661f, 168.583f, 324.164f, (byte) 3));
		balaur.add((Npc) QuestService.spawnQuestNpc(600030000, player.getInstanceId(), 218825, 2453.150f, 174.048f, 323.503f, (byte) 0));
		for (Npc mob : balaur) {
			mob.setTarget(npc);
			mob.getAggroList().addHate(npc, 1);
		}
	}
}
