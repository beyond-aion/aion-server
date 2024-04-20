package quest.altgard;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Artur, Ritsu, Majka
 */
public class _24013PoisonInTheWaters extends AbstractQuestHandler {

	private final static int[] mobs = { 210455, 210456, 214039, 210458, 214032 };

	public _24013PoisonInTheWaters() {
		super(24013);
	}

	@Override
	public void register() {
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		qe.registerQuestItem(182215359, questId);
		qe.registerQuestNpc(203631).addOnTalkEvent(questId);
		qe.registerQuestNpc(203621).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		final int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203631:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							break;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
					return false;
				case 203621:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
							break;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2, 182215359, 1); // 2
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203631) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		if (player.isInsideZone(ZoneName.get("DF1A_ITEMUSEAREA_Q2016_220030000"))) {

			// Spawns 2 Feral Black Claw Sharpeye [ID: 210457] far from the player
			ThreadPoolManager.getInstance().schedule(() -> {
				float playerX = env.getPlayer().getX();
				float playerY = env.getPlayer().getY();
				float playerZ = env.getPlayer().getZ();
				spawn(210457, player, playerX + 13.0f, playerY - 3.0f, playerZ, (byte) 60); // Right
				spawn(210457, player, playerX + 13.0f, playerY + 3.0f, playerZ, (byte) 60); // Left
			}, 3000);

			return HandlerResult.fromBoolean(useQuestItem(env, item, 2, 3, false)); // 3
		}
		return HandlerResult.UNKNOWN;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		if (defaultOnKillEvent(env, mobs, 7, true))
			return true;
		return defaultOnKillEvent(env, mobs, 3, 7); // 6
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 24010);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 24010);
	}
}
