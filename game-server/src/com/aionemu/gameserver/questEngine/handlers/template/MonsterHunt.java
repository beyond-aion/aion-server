package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.rift.RiftLocation;
import com.aionemu.gameserver.model.templates.quest.QuestCategory;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.handlers.models.Monster;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.RiftService;
import com.aionemu.gameserver.services.VortexService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke
 * @reworked vlog, Bobobear, Pad
 */
public class MonsterHunt extends QuestHandler {

	private static final Logger log = LoggerFactory.getLogger(MonsterHunt.class);

	private final Set<Integer> startNpcs = new HashSet<Integer>();
	private final Set<Integer> endNpcs = new HashSet<Integer>();
	private final FastMap<Monster, Set<Integer>> monsters;
	private final int startDialog;
	private final int endDialog;
	private final Set<Integer> aggroNpcs = new HashSet<Integer>();
	private final int invasionWorldId;
	private QuestItems workItem;
	private final int startDistanceNpc;

	public MonsterHunt(int questId, List<Integer> startNpcIds, List<Integer> endNpcIds, FastMap<Monster, Set<Integer>> monsters, int startDialog,
		int endDialog, List<Integer> aggroNpcs, int invasionWorld, int startDistanceNpc) {
		super(questId);
		this.startNpcs.addAll(startNpcIds);
		this.startNpcs.remove(0);
		if (endNpcIds == null) {
			this.endNpcs.addAll(startNpcs);
		} else {
			this.endNpcs.addAll(endNpcIds);
			this.endNpcs.remove(0);
		}
		this.monsters = monsters;
		this.startDialog = startDialog;
		this.endDialog = endDialog;
		if (aggroNpcs != null) {
			this.aggroNpcs.addAll(aggroNpcs);
			this.aggroNpcs.remove(0);
		}
		this.invasionWorldId = invasionWorld;
		this.startDistanceNpc = startDistanceNpc;
	}

	@Override
	protected void onWorkItemsLoaded() {
		if (workItems == null)
			return;
		if (workItems.size() > 1) {
			log.warn("Q{} (MonsterHunt) has more than 1 work item.", questId);
		}
		workItem = workItems.get(0);
	}

	@Override
	public void register() {
		for (Integer startNpc : startNpcs) {
			qe.registerQuestNpc(startNpc).addOnQuestStart(getQuestId());
			qe.registerQuestNpc(startNpc).addOnTalkEvent(getQuestId());
		}

		for (Set<Integer> monsterIds : monsters.values()) {
			for (Integer monsterId : monsterIds)
				qe.registerQuestNpc(monsterId).addOnKillEvent(questId);
		}

		for (Integer endNpc : endNpcs)
			qe.registerQuestNpc(endNpc).addOnTalkEvent(getQuestId());

		for (Integer aggroNpc : aggroNpcs)
			qe.registerQuestNpc(aggroNpc).addOnAddAggroListEvent(getQuestId());

		if (invasionWorldId != 0)
			qe.registerOnEnterWorld(questId);

		if (startDistanceNpc != 0)
			qe.registerQuestNpc(startDistanceNpc, 300).addOnAtDistanceEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (startNpcs.isEmpty() || startNpcs.contains(targetId)
				|| DataManager.QUEST_DATA.getQuestById(questId).getCategory() == QuestCategory.FACTION) {
				if (env.getDialog() == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, startDialog != 0 ? startDialog : 1011);
				} else {
					switch (env.getDialog()) {
						case QUEST_REFUSE_1:
						case FINISH_DIALOG:
							return closeDialogWindow(env);
						case QUEST_ACCEPT:
						case QUEST_ACCEPT_1:
						case QUEST_ACCEPT_SIMPLE:
							if (workItem != null) {
								// Some quest work items come from other quests, don't add again
								long currentCount = workItem.getCount();
								currentCount -= player.getInventory().getItemCountByItemId(workItem.getItemId());
								if (currentCount > 0)
									giveQuestItem(env, workItem.getItemId(), currentCount, ItemAddType.QUEST_WORK_ITEM);
							}
						default: {
							return sendQuestStartDialog(env);
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (endNpcs.contains(targetId)) {
				if (env.getDialog() == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, endDialog != 0 ? endDialog : 1352);
				} else if (env.getDialog() == DialogAction.SELECT_QUEST_REWARD) {
					for (Monster mi : monsters.keySet()) {
						int endVar = mi.getEndVar();
						int varId = mi.getVar();
						int total = 0;
						do {
							int currentVar = qs.getQuestVarById(varId);
							total += currentVar << ((varId - mi.getVar()) * 6);
							endVar >>= 6;
							varId++;
						} while (endVar > 0);
						if (mi.getEndVar() > total) {
							if (player.getAccessLevel() >= 3 && CustomConfig.ENABLE_SHOW_DIALOGID) {
								PacketSendUtility.sendMessage(player, "varId: " + varId + "; req endVar: " + mi.getEndVar() + "; curr total: " + total);
							}
							return false;
						}
					}
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (endNpcs.contains(targetId)) {
				if (!aggroNpcs.isEmpty()) {
					switch (env.getDialog()) {
						case QUEST_SELECT:
						case USE_OBJECT:
							return sendQuestDialog(env, 10002);
						case SELECT_QUEST_REWARD: {
							if (workItem != null) {
								long currentCount = player.getInventory().getItemCountByItemId(workItem.getItemId());
								if (currentCount > 0)
									removeQuestItem(env, workItem.getItemId(), currentCount, QuestStatus.COMPLETE);
							}
							return sendQuestDialog(env, 5);
						}
						default:
							return sendQuestEndDialog(env);
					}
				} else
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
			for (Monster m : monsters.keySet()) {
				if (m.getNpcIds().contains(env.getTargetId())) {
					int endVar = m.getEndVar();
					int varId = m.getVar();
					int total = 0;
					do {
						int currentVar = qs.getQuestVarById(varId);
						total += currentVar << ((varId - m.getVar()) * 6);
						endVar >>= 6;
						varId++;
					} while (endVar > 0);
					total += 1;
					if (total <= m.getEndVar()) {
						if (!aggroNpcs.isEmpty()) {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
						} else {
							for (int varsUsed = m.getVar(); varsUsed < varId; varsUsed++) {
								int value = total & 0x3F;
								total >>= 6;
								qs.setQuestVarById(varsUsed, value);
							}
							if (qs.getQuestVarById(m.getVar()) == m.getEndVar()) {
								if (m.getRewardVar()) {
									qs.setStatus(QuestStatus.REWARD);
								} else if (m.getRewardNextStep()) {
									qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
									qs.setStatus(QuestStatus.REWARD);
								}
							}
							updateQuestStatus(env);
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onAddAggroListEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			QuestService.startQuest(env);
			return true;
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		VortexLocation vortexLoc = VortexService.getInstance().getLocationByWorld(invasionWorldId);
		if (player.getWorldId() == invasionWorldId) {
			if ((qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat())) {
				if ((vortexLoc != null && vortexLoc.isActive()) || (searchOpenRift()))
					return QuestService.startQuest(env);
			}
		}
		return false;
	}

	private boolean searchOpenRift() {
		for (RiftLocation loc : RiftService.getInstance().getRiftLocations().values()) {
			if (loc.getWorldId() == invasionWorldId && loc.isOpened()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onAtDistanceEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			QuestService.startQuest(env);
			return true;
		}
		return false;
	}

	@Override
	public HashSet<Integer> getNpcIds() {
		if (constantSpawns == null) {
			constantSpawns = new HashSet<>();
			if (startNpcs != null)
				constantSpawns.addAll(startNpcs);
			if (endNpcs != null)
				constantSpawns.addAll(endNpcs);
			if (aggroNpcs != null)
				constantSpawns.addAll(aggroNpcs);
			for (Set<Integer> mobIds : monsters.values())
				constantSpawns.addAll(mobIds);
		}
		return constantSpawns;
	}

}
