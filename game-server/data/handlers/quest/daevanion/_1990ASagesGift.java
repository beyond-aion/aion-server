package quest.daevanion;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author kecimis, Tiger, vlog, Neon
 */
public class _1990ASagesGift extends AbstractQuestHandler {

	public _1990ASagesGift() {
		super(1990);
	}

	@Override
	public void register() {
		int[] mobs = { 256617, 253721, 253720, 254514, 254513 };
		qe.registerQuestNpc(203771).addOnQuestStart(questId);
		qe.registerQuestNpc(203771).addOnTalkEvent(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203771) { // Fermina
				if (env.getDialogActionId() == QUEST_SELECT) {
					if (isDaevanionArmorEquipped(player)) {
						return sendQuestDialog(env, 4762);
					} else {
						return sendQuestDialog(env, 4848);
					}
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			int var1 = qs.getQuestVarById(1);
			if (targetId == 203771) { // Fermina
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						} else if (var == 1) {
							return sendQuestDialog(env, 10000);
						} else if (var == 2 && var1 == 60) {
							return sendQuestDialog(env, 1693);
						} else if (var == 3) {
							return sendQuestDialog(env, 2034);
						}
						return false;
					case CHECK_USER_HAS_QUEST_ITEM:
						return checkQuestItems(env, 0, 1, false, 10000, 10001); // 1
					case SELECT4_1:
						int currentDp = player.getCommonData().getDp();
						int maxDp = player.getGameStats().getMaxDp().getCurrent();
						long burner = player.getInventory().getItemCountByItemId(186000040); // Divine Incense Burner
						if (currentDp == maxDp && burner >= 1) {
							removeQuestItem(env, 186000040, 1);
							player.getCommonData().setDp(0);
							changeQuestStep(env, 3, 3, true); // reward
							return sendQuestDialog(env, 5);
						} else {
							return sendQuestDialog(env, 2120);
						}
					case SETPRO2:
						return defaultCloseDialog(env, 1, 2); // 2
					case SETPRO3:
						qs.setQuestVar(3); // 3
						updateQuestStatus(env);
						return sendQuestSelectionDialog(env);
					case FINISH_DIALOG:
						return sendQuestSelectionDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203771) { // Fermina
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
			int all;
			int a = (qs.getQuestVars().getQuestVars() >> 7) & 0x7F;
			int b = (qs.getQuestVars().getQuestVars() >> 14) & 0x7F;
			int c = (qs.getQuestVars().getQuestVars() >> 21) & 0x7F;
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				switch (env.getTargetId()) {
					case 256617: // Strange Lake Spirit
						if (a >= 0 && a < 30) {
							++a;
							all = c;
							all = all << 7;
							all += b;
							all = all << 7;
							all += a;
							all = all << 7;
							all += 2;// var0
							qs.setQuestVar(all);
							updateQuestStatus(env);
						}
						break;
					case 253721:
					case 253720: // Lava Hoverstone
						if (b >= 0 && b < 30) {
							++b;
							all = c;
							all = all << 7;
							all += b;
							all = all << 7;
							all += a;
							all = all << 7;
							all += 2;// var0
							qs.setQuestVar(all);
							updateQuestStatus(env);
						}
						break;
					case 254514:
					case 254513: // Disturbed Resident
						if (c >= 0 && c < 30) {
							++c;
							all = c;
							all = all << 7;
							all += b;
							all = all << 7;
							all += a;
							all = all << 7;
							all += 2;// var0
							qs.setQuestVar(all);
							updateQuestStatus(env);
						}
						break;
				}
				if (qs.getQuestVarById(0) == 2 && a == 30 && b == 30 && c == 30) {
					qs.setQuestVarById(1, 60);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}

	private boolean isDaevanionArmorEquipped(Player player) {
		int plate = player.getEquipment().itemSetPartsEquipped(9);
		int chain = player.getEquipment().itemSetPartsEquipped(8);
		int leather = player.getEquipment().itemSetPartsEquipped(7);
		int cloth = player.getEquipment().itemSetPartsEquipped(6);
		int gunner = player.getEquipment().itemSetPartsEquipped(378);
		if (plate == 5 || chain == 5 || leather == 5 || cloth == 5 || gunner == 5) {
			return true;
		}
		return false;
	}
}
