package com.aionemu.gameserver.questEngine.handlers.template;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.rift.RiftLocation;
import com.aionemu.gameserver.model.templates.quest.QuestCategory;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.questEngine.handlers.models.Monster;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.RiftService;
import com.aionemu.gameserver.services.VortexService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author MrPoke, vlog, Bobobear, Pad, Majka
 */
public class MonsterHunt extends AbstractTemplateQuestHandler {

	private static final Logger log = LoggerFactory.getLogger(MonsterHunt.class);

	private final Set<Integer> startNpcIds = new HashSet<>();
	private final Set<Integer> endNpcIds = new HashSet<>();
	private final List<Monster> monsters;
	private final int startDialogId;
	private final int endDialogId;
	private final Set<Integer> aggroNpcIds = new HashSet<>();
	private final int invasionWorldId;
	private QuestItems workItem;
	private final String startZone;
	private final int startDistanceNpcId;
	private final boolean reward;
	private final boolean rewardNextStep;
	private final boolean isDataDriven;

	public MonsterHunt(int questId, List<Integer> startNpcIds, List<Integer> endNpcIds, List<Monster> monsters, int startDialogId,
		int endDialogId, List<Integer> aggroNpcIds, int invasionWorld, String startZone, int startDistanceNpcId, boolean reward, boolean rewardNextStep) {
		super(questId);
		if (startNpcIds != null)
			this.startNpcIds.addAll(startNpcIds);
		if (endNpcIds != null)
			this.endNpcIds.addAll(endNpcIds);
		else
			this.endNpcIds.addAll(this.startNpcIds);
		this.monsters = monsters;
		this.startDialogId = startDialogId;
		this.endDialogId = endDialogId;
		if (aggroNpcIds != null)
			this.aggroNpcIds.addAll(aggroNpcIds);
		this.invasionWorldId = invasionWorld;
		this.startZone = startZone;
		this.startDistanceNpcId = startDistanceNpcId;
		this.reward = reward;
		this.rewardNextStep = rewardNextStep;
		this.isDataDriven = DataManager.QUEST_DATA.getQuestById(questId).isDataDriven();
		if (workItems != null) {
			if (workItems.size() > 1)
				log.warn("Q{} has more than 1 work item", questId);
			workItem = workItems.get(0);
		}
	}

	@Override
	public void register() {
		for (Integer startNpcId : startNpcIds) {
			qe.registerQuestNpc(startNpcId).addOnQuestStart(questId);
			qe.registerQuestNpc(startNpcId).addOnTalkEvent(questId);
		}

		for (Monster monster : monsters) {
			for (Integer monsterId : monster.getNpcIds())
				qe.registerQuestNpc(monsterId).addOnKillEvent(questId);
		}

		for (Integer endNpcId : endNpcIds)
			qe.registerQuestNpc(endNpcId).addOnTalkEvent(questId);

		for (Integer aggroNpcId : aggroNpcIds)
			qe.registerQuestNpc(aggroNpcId).addOnAddAggroListEvent(questId);

		if (invasionWorldId != 0)
			qe.registerOnEnterWorld(questId);

		if (startZone != null && !ZoneName.get(startZone).name().equalsIgnoreCase("NONE"))
			qe.registerOnEnterZone(ZoneName.get(startZone), questId);

		if (startDistanceNpcId != 0)
			qe.registerQuestNpc(startDistanceNpcId, 300).addOnAtDistanceEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (startNpcIds.isEmpty() || startNpcIds.contains(targetId)
				|| DataManager.QUEST_DATA.getQuestById(questId).getCategory() == QuestCategory.FACTION) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, startDialogId != 0 ? startDialogId : isDataDriven ? 4762 : 1011);
					case QUEST_ACCEPT:
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env, workItem);
					default:
						return super.onDialogEvent(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (endNpcIds.contains(targetId)) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, endDialogId != 0 ? endDialogId : 1352);
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					for (Monster mi : monsters) {
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
							if (player.hasAccess(AdminConfig.DIALOG_INFO))
								PacketSendUtility.sendMessage(player, "varId: " + varId + "; req endVar: " + mi.getEndVar() + "; curr total: " + total);
							return false;
						}
					}
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (endNpcIds.contains(targetId)) {
				if (!aggroNpcIds.isEmpty() || isDataDriven) {
					switch (dialogActionId) {
						case QUEST_SELECT:
						case USE_OBJECT:
							return sendQuestDialog(env, 10002);
						case SELECT_QUEST_REWARD:
							if (workItem != null) {
								long currentCount = player.getInventory().getItemCountByItemId(workItem.getItemId());
								if (currentCount > 0)
									removeQuestItem(env, workItem.getItemId(), currentCount, QuestStatus.COMPLETE);
							}
					}
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
			int currentTotalVar = 0;
			int totalEndVar = 0;
			int curStep = qs.getQuestVarById(0);
			int lastStep = 0;

			for (Monster m : monsters) {
				lastStep = Math.max(lastStep, m.getStep());
				if (isDataDriven && m.getStep() != curStep) // Check only for current step for new style quests
					continue;
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
						if (!aggroNpcIds.isEmpty()) {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return true;
						} else {
							int tmpTotal = total;
							for (int varsUsed = m.getVar(); varsUsed < varId; varsUsed++) {
								int value = total & 0x3F;
								total >>= 6;
								qs.setQuestVarById(varsUsed, value);
							}
							updateQuestStatus(env);
							if (!isDataDriven) { // Old quest style
								if (tmpTotal == m.getEndVar() && (reward || rewardNextStep)) {
									if (rewardNextStep)
										qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
									qs.setStatus(QuestStatus.REWARD);
									updateQuestStatus(env);
								}
								return true;
							}
						}
					}
				}
				// Totals for quest step
				totalEndVar += m.getEndVar();
				currentTotalVar += qs.getQuestVarById(m.getVar());
			}

			// Checks if step is completed
			if (currentTotalVar >= totalEndVar && isDataDriven) { // New quest style
				qs.setQuestVar(curStep + 1);
				if (curStep >= lastStep) {
					qs.setStatus(QuestStatus.REWARD);
				}
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onAddAggroListEvent(QuestEnv env) {
		return startQuest(env);
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		VortexLocation vortexLoc = VortexService.getInstance().getLocationByWorld(invasionWorldId);
		if (player.getWorldId() == invasionWorldId) {
			if (qs == null || qs.isStartable()) {
				if (vortexLoc != null && vortexLoc.isActive() || searchOpenRift())
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
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName.name().equalsIgnoreCase(startZone))
			return startQuest(env);
		return false;
	}

	@Override
	public boolean onAtDistanceEvent(QuestEnv env) {
		return startQuest(env);
	}

	public boolean startQuest(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			return QuestService.startQuest(env);
		}
		return false;
	}
}
