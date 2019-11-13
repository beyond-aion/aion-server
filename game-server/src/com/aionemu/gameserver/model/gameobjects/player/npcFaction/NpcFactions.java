package com.aionemu.gameserver.model.gameobjects.player.npcFaction;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.factions.FactionCategory;
import com.aionemu.gameserver.model.templates.factions.NpcFactionTemplate;
import com.aionemu.gameserver.model.templates.quest.QuestMentorType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TITLE_INFO;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author MrPoke, synchro2, Sykra
 */
public class NpcFactions {

	private Player owner;

	private Map<Integer, NpcFaction> factions = new HashMap<>();
	private NpcFaction[] activeNpcFaction = new NpcFaction[2];
	private int[] timeLimit = new int[] { 0, 0 };

	public NpcFactions(Player owner) {
		this.owner = owner;
	}

	public void addNpcFaction(NpcFaction faction) {
		factions.put(faction.getId(), faction);
		int type = 0;
		if (faction.isMentor())
			type = 1;

		if (faction.isActive())
			activeNpcFaction[type] = faction;
		if (faction.getTime() == -1) {
			// used to reset from quest daily command
			faction.setTime((int) (System.currentTimeMillis() / 1000));
			timeLimit[type] = faction.getTime();
		} else if (timeLimit[type] < faction.getTime() && faction.getState() == ENpcFactionQuestState.COMPLETE)
			timeLimit[type] = faction.getTime();
	}

	public NpcFaction getFactionById(int id) {
		return factions.get(id);
	}

	public Collection<NpcFaction> getNpcFactions() {
		return factions.values();
	}

	public NpcFaction getActiveNpcFaction(boolean mentor) {
		return activeNpcFaction[mentor ? 1 : 0];
	}

	public NpcFaction setActive(int npcFactionId) {
		NpcFaction npcFaction = factions.computeIfAbsent(npcFactionId, factionId -> new NpcFaction(factionId, 0, false, ENpcFactionQuestState.NOTING, 0));
		npcFaction.setActive(true);
		activeNpcFaction[npcFaction.isMentor() ? 1 : 0] = npcFaction;
		return npcFaction;
	}

	public void leaveNpcFaction(Npc npc) {
		int targetObjectId = npc.getObjectId();
		NpcFactionTemplate npcFactionTemplate = DataManager.NPC_FACTIONS_DATA.getNpcFactionByNpcId(npc.getNpcId());
		if (npcFactionTemplate == null)
			return;
		NpcFaction npcFaction = getFactionById(npcFactionTemplate.getId());
		if (npcFaction == null || !npcFaction.isActive()) {
			PacketSendUtility.sendPacket(owner, new SM_DIALOG_WINDOW(targetObjectId, 1438));
			return;
		}
		PacketSendUtility.sendPacket(owner, new SM_DIALOG_WINDOW(targetObjectId, 1353));
		leaveNpcFaction(npcFaction);
	}

	private void leaveNpcFaction(NpcFaction npcFaction) {
		NpcFactionTemplate npcFactionTemplate = DataManager.NPC_FACTIONS_DATA.getNpcFactionById(npcFaction.getId());

		PacketSendUtility.sendPacket(owner, SM_SYSTEM_MESSAGE.STR_FACTION_LEAVE(npcFactionTemplate.getL10n()));
		npcFaction.setActive(false);
		activeNpcFaction[npcFactionTemplate.isMentor() ? 1 : 0] = null;
		if (npcFaction.getState() == ENpcFactionQuestState.START) {
			QuestService.abandonQuest(owner, npcFaction.getQuestId());
			npcFaction.setState(ENpcFactionQuestState.NOTING);
		}
	}

	public void enterGuild(Npc npc) {
		int targetObjectId = npc.getObjectId();
		NpcFactionTemplate npcFactionTemplate = DataManager.NPC_FACTIONS_DATA.getNpcFactionByNpcId(npc.getNpcId());
		if (npcFactionTemplate == null) {
			LoggerFactory.getLogger(NpcFactions.class).warn("Missing faction for faction registrar npc " + npc.getNpcId());
			return;
		}
		NpcFaction npcFaction = getFactionById(npcFactionTemplate.getId());
		NpcFaction activeNpcFaction = getActiveNpcFaction(npcFactionTemplate.isMentor());
		int npcFactionId = npcFactionTemplate.getId();
		int skillPoints = npcFactionTemplate.getSkillPoints();
		if (skillPoints != 0) {
			boolean canEnter = false;
			if (npcFactionTemplate.getCategory() == FactionCategory.COMBINESKILL) {
				for (PlayerSkillEntry skill : owner.getSkillList().getAllSkills()) {
					if (skill.isCraftingSkill() && skill.getSkillLevel() >= skillPoints) {
						canEnter = true;
						break;
					}
				}
			}
			if (!canEnter) {
				PacketSendUtility.sendPacket(owner, new SM_DIALOG_WINDOW(targetObjectId, 1098));
				return;
			}
		}
		if (owner.getLevel() < npcFactionTemplate.getMinLevel() || owner.getLevel() > npcFactionTemplate.getMaxLevel()) {
			PacketSendUtility.sendPacket(owner, new SM_DIALOG_WINDOW(targetObjectId, 1182));
			return;
		}
		if (owner.getRace() != npcFactionTemplate.getRace() && !npcFactionTemplate.getRace().equals(Race.NPC)) {
			PacketSendUtility.sendPacket(owner, new SM_DIALOG_WINDOW(targetObjectId, 1097));
			return;
		}
		if (npcFaction != null && npcFaction.isActive()) {
			PacketSendUtility.sendPacket(owner, SM_SYSTEM_MESSAGE.STR_FACTION_CAN_NOT_JOIN());
			return;
		}
		if (activeNpcFaction != null && activeNpcFaction.getId() != npcFactionId) {
			askLeaveNpcFaction(npc);
			return;
		}
		if (npcFaction == null || !npcFaction.isActive()) {
			PacketSendUtility.sendPacket(owner, SM_SYSTEM_MESSAGE.STR_FACTION_JOIN(npcFactionTemplate.getL10n()));
			PacketSendUtility.sendPacket(owner, new SM_DIALOG_WINDOW(targetObjectId, 1012));
			setActive(npcFactionId);
			sendDailyQuest();
		}
	}

	private void askLeaveNpcFaction(final Npc npc) {
		NpcFactionTemplate npcFactionTemplate = DataManager.NPC_FACTIONS_DATA.getNpcFactionByNpcId(npc.getNpcId());
		final NpcFaction activeNpcFaction = getActiveNpcFaction(npcFactionTemplate.isMentor());
		NpcFactionTemplate activeNpcFactionTemplate = DataManager.NPC_FACTIONS_DATA.getNpcFactionById(activeNpcFaction.getId());
		RequestResponseHandler<Player> responseHandler = new RequestResponseHandler<Player>(owner) {

			@Override
			public void acceptRequest(Player requester, Player responder) {
				leaveNpcFaction(activeNpcFaction);
				enterGuild(npc);
			}
		};
		boolean requested = owner.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_ASK_JOIN_NEW_FACTION, responseHandler);
		if (requested) {
			PacketSendUtility.sendPacket(owner,
				new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_ASK_JOIN_NEW_FACTION, 0, 0, activeNpcFactionTemplate.getL10n(), npcFactionTemplate.getL10n()));
		}
	}

	public void startQuest(QuestTemplate questTemplate) {
		NpcFaction npcFaction = activeNpcFaction[questTemplate.isMentor() ? 1 : 0];
		if (npcFaction == null)
			return;
		if (npcFaction.getState() != ENpcFactionQuestState.NOTING && npcFaction.getQuestId() == 0)
			return;
		npcFaction.setState(ENpcFactionQuestState.START);
	}

	public void abortQuest(QuestTemplate questTemplate) {
		NpcFaction npcFaction = factions.get(questTemplate.getNpcFactionId());
		if (npcFaction == null || !npcFaction.isActive())
			return;
		npcFaction.setState(ENpcFactionQuestState.NOTING);
		sendDailyQuest();
	}

	public void completeQuest(QuestTemplate questTemplate) {
		NpcFaction npcFaction = activeNpcFaction[questTemplate.isMentor() ? 1 : 0];
		if (npcFaction == null)
			return;
		npcFaction.setTime(getNextTime());
		npcFaction.setState(ENpcFactionQuestState.COMPLETE);
		timeLimit[npcFaction.isMentor() ? 1 : 0] = npcFaction.getTime();
		if (questTemplate.getMentorType() == QuestMentorType.MENTOR) {
			owner.getCommonData().setMentorFlagTime((int) (System.currentTimeMillis() / 1000) + 60 * 60 * 24); // TODO 1 day
			PacketSendUtility.broadcastPacket(owner, new SM_TITLE_INFO(owner, true), false);
			PacketSendUtility.sendPacket(owner, new SM_TITLE_INFO(true));
		}
	}

	public void sendDailyQuest() {
		for (int i = 0; i < 2; i++) {
			NpcFaction faction = activeNpcFaction[i];
			if (faction == null || !faction.isActive())
				continue;
			if (timeLimit[i] > System.currentTimeMillis() / 1000)
				continue;
			int questId = 0;
			switch (faction.getState()) {
				case COMPLETE:
					if (faction.getTime() > System.currentTimeMillis() / 1000)
						continue;
					break;
				case START:
					continue;
				case NOTING:
					if (faction.getTime() > System.currentTimeMillis() / 1000)
						questId = faction.getQuestId();
					break;
			}

			if (questId == 0) {
				List<QuestTemplate> quests = DataManager.QUEST_DATA.getQuestsByNpcFaction(faction.getId(), owner);
				if (quests.isEmpty())
					continue;
				questId = Rnd.get(quests).getId();
				faction.setQuestId(questId);
				faction.setTime(getNextTime());
				faction.setState(ENpcFactionQuestState.NOTING);
			}
			PacketSendUtility.sendPacket(owner, new SM_QUEST_ACTION(questId));
		}
	}

	public void onLevelUp() {
		for (int i = 0; i < 2; i++) {
			NpcFaction faction = activeNpcFaction[i];
			if (faction == null || !faction.isActive())
				continue;
			NpcFactionTemplate npcFactionTemplate = DataManager.NPC_FACTIONS_DATA.getNpcFactionById(faction.getId());
			if (npcFactionTemplate.getMaxLevel() < owner.getLevel()) {
				faction.setActive(false);
				activeNpcFaction[i] = null;
				if (faction.getState() == ENpcFactionQuestState.START)
					QuestService.abandonQuest(owner, faction.getQuestId());
				PacketSendUtility.sendPacket(owner, SM_SYSTEM_MESSAGE.STR_FACTION_LEAVE_BY_LEVEL_LIMIT(npcFactionTemplate.getL10n()));
				faction.setState(ENpcFactionQuestState.NOTING);
			}
		}
	}

	private int getNextTime() {
		ZonedDateTime now = ServerTime.now();
		long repeatDateEpochSeconds;
		if (now.getHour() >= 9)
			repeatDateEpochSeconds = now.with(LocalTime.MIDNIGHT).withHour(9).plusDays(1).toEpochSecond(); // tomorrow morning at 9:00 AM
		else
			repeatDateEpochSeconds = now.with(LocalTime.MIDNIGHT).withHour(9).toEpochSecond(); // today morning at 9:00 AM
		return (int) repeatDateEpochSeconds;
	}

	public boolean canStartQuest(QuestTemplate template) {
		int type = template.isMentor() ? 1 : 0;
		return activeNpcFaction[type] != null && timeLimit[type] < System.currentTimeMillis() / 1000;
	}
}
