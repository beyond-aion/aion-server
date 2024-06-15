package com.aionemu.gameserver.services.event;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dao.EventDAO;
import com.aionemu.gameserver.dao.EventDAO.StoredBuffData;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.InstanceCooltime;
import com.aionemu.gameserver.model.templates.event.Buff;
import com.aionemu.gameserver.model.templates.event.Buff.BuffMapType;
import com.aionemu.gameserver.model.templates.event.Buff.Trigger;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Effect.ForceType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.time.ServerTime;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Neon
 */
public class EventBuffHandler {

	private final String eventName;
	private final List<Buff> buffs;
	private final Map<Buff, Set<Integer>> activeBuffPoolSkillIds = new ConcurrentHashMap<>();
	private final Map<Buff, Set<Integer>> allowedBuffDays = new ConcurrentHashMap<>();
	private volatile int dayOfMonth = ServerTime.now().getDayOfMonth();
	private final ForceType effectForceType;

	public EventBuffHandler(String eventName, List<Buff> buffs) {
		this.eventName = eventName;
		this.buffs = buffs;
		this.effectForceType = Event.getOrCreateEffectForceType(eventName);
		initBuffData();
	}

	private void initBuffData() {
		updateActiveBuffSkillIds();
		updateAllowedBuffDays(ServerTime.now().toLocalDate().lengthOfMonth());
		List<StoredBuffData> buffData = EventDAO.loadStoredBuffData(eventName);
		if (buffData != null) {
			for (StoredBuffData storedBuffData : buffData) {
				if (storedBuffData.getBuffIndex() < buffs.size()) {
					Buff buff = buffs.get(storedBuffData.getBuffIndex());
					if (storedBuffData.getActivePoolSkillIds() != null && buff.getSkillIds().containsAll(storedBuffData.getActivePoolSkillIds()))
						activeBuffPoolSkillIds.put(buff, storedBuffData.getActivePoolSkillIds());
					if (storedBuffData.getAllowedBuffDays() != null && buff.getRestriction() != null
						&& buff.getRestriction().getRandomDaysPerMonth() == storedBuffData.getAllowedBuffDays().size())
						allowedBuffDays.put(buff, storedBuffData.getAllowedBuffDays());
				}
			}
		}
		storeBuffDataInDb();
	}

	private void updateActiveBuffSkillIds() {
		for (Buff buff : buffs) {
			if (buff.getPool() > 0)
				activeBuffPoolSkillIds.put(buff, collectNRandomElements(buff.getSkillIds(), buff.getPool()));
		}
	}

	private void updateAllowedBuffDays(int endOfMonthDay) {
		buffs.forEach(buff -> {
			int limit = buff.getRestriction() == null ? 0 : buff.getRestriction().getRandomDaysPerMonth();
			if (limit > 0 && limit < endOfMonthDay) {
				List<Integer> daysThisMonth = IntStream.rangeClosed(1, endOfMonthDay).boxed().collect(Collectors.toList());
				Set<Integer> allowedRandomDaysThisMonth = collectNRandomElements(daysThisMonth, limit);
				allowedBuffDays.put(buff, allowedRandomDaysThisMonth);
			} else {
				allowedBuffDays.remove(buff);
			}
		});
	}

	private void storeBuffDataInDb() {
		List<StoredBuffData> storedBuffData = new ArrayList<>(buffs.size());
		for (int i = 0; i < buffs.size(); i++) {
			Buff buff = buffs.get(i);
			Set<Integer> poolSkillIds = activeBuffPoolSkillIds.get(buff);
			Set<Integer> allowedDays = allowedBuffDays.get(buff);
			if (poolSkillIds != null && allowedDays != null)
				storedBuffData.add(new StoredBuffData(i, poolSkillIds, allowedDays));
		}
		EventDAO.storeBuffData(eventName, storedBuffData);
	}

	public ForceType getEffectForceType() {
		return effectForceType;
	}

	public void onTimeChanged(ZonedDateTime now) {
		int nowDayOfMonth = now.getDayOfMonth();
		if (nowDayOfMonth != dayOfMonth) {
			dayOfMonth = nowDayOfMonth;
			updateActiveBuffSkillIds();
			if (dayOfMonth == 1)
				updateAllowedBuffDays(now.toLocalDate().lengthOfMonth());
			storeBuffDataInDb();
			resetTodaysBuffs();
		}
	}

	private void resetTodaysBuffs() {
		World.getInstance().forEachPlayer(this::onEnterMap);
	}

	public void onEventStop() {
		World.getInstance().forEachPlayer(this::endEventBuffs);
	}

	public void onEnterMap(Player player) {
		endRestrictedEventBuffs(player);
		tryBuff(player, Buff.TriggerCondition.ENTER_MAP);
	}

	public void onEnteredTeam(Player player, TemporaryPlayerTeam<? extends TeamMember<Player>> team) {
		team.forEach(this::endRestrictedEventBuffs);
		tryBuff(player, Buff.TriggerCondition.ENTER_TEAM);
	}

	public void onLeftTeam(Player player, TemporaryPlayerTeam<? extends TeamMember<Player>> team) {
		endRestrictedEventBuffs(player); // player isn't in team anymore
		team.forEach(member -> {
			endRestrictedEventBuffs(member);
			// try to apply buffs since some restrictions are now maybe met (team_size_max_percent)
			buffs.forEach(buff -> tryBuff(buff, member, Buff.TriggerCondition.ENTER_TEAM));
		});
	}

	public void onPveKill(Player killer, Npc victim) {
		if (killer.getLevel() - victim.getLevel() < 10) // victim can be 9 levels below killer level
			tryBuff(killer, Buff.TriggerCondition.PVE_KILL);
	}

	public void onPvpKill(Player killer, Player victim) {
		tryBuff(killer, Buff.TriggerCondition.PVP_KILL);
	}

	private void endEventBuffs(Player player) {
		player.getEffectController().getAllEffects().forEach(effect -> {
			if (effect.getForceType() == effectForceType)
				effect.endEffect();
		});
	}

	private void endRestrictedEventBuffs(Player player) {
		player.getEffectController().getAllEffects().forEach(effect -> {
			if (effect.getForceType() == effectForceType) {
				for (Buff buff : buffs) {
					if (getActiveBuffSkillIds(buff).contains(effect.getSkillId()) && checkRestrictions(buff, player))
						return; // event effect is still valid, check next one
				}
				effect.endEffect();
			}
		});
	}

	private boolean applyOnTeam(Player player, Consumer<Player> memberAction) {
		TemporaryPlayerTeam<?> team = player.getCurrentTeam();
		if (team != null) {
			team.forEach(memberAction);
			return true;
		}
		return false;
	}

	private void tryBuff(Player player, Buff.TriggerCondition triggerCondition) {
		buffs.forEach(buff -> {
			if (!buff.isTeam() || !applyOnTeam(player, member -> tryBuff(buff, member, triggerCondition)))
				tryBuff(buff, player, triggerCondition);
		});
	}

	private void tryBuff(Buff buff, Player player, Buff.TriggerCondition triggerCondition) {
		if (canReceiveBuff(buff, player, triggerCondition)) {
			for (int skillId : getActiveBuffSkillIds(buff)) {
				if (player.getEffectController().hasAbnormalEffect(skillId))
					continue;
				Effect effect = SkillEngine.getInstance().applyEffectDirectly(skillId, player, player, buff.isPermanent() ? 0 : null, effectForceType);
				if (effect != null) {
					int msgId = 1400697; // You received %0: %1.
					SM_SYSTEM_MESSAGE message = new SM_SYSTEM_MESSAGE(ChatType.BRIGHT_YELLOW_CENTER, player, msgId, "[Server Buff]",
						effect.getSkillTemplate().getL10n());
					PacketSendUtility.sendPacket(player, message);
				}
			}
		}
	}

	private Set<Integer> getActiveBuffSkillIds(Buff buff) {
		return activeBuffPoolSkillIds.getOrDefault(buff, buff.getSkillIds());
	}

	private <T> Set<T> collectNRandomElements(Collection<T> input, int n) {
		List<T> shuffledInput = new ArrayList<>(input);
		Collections.shuffle(shuffledInput);
		Set<T> nRandomElements = new HashSet<>(n);
		for (int i = 0; i < n; i++)
			nRandomElements.add(shuffledInput.get(i));
		return nRandomElements;
	}

	private boolean canReceiveBuff(Buff buff, Player player, Buff.TriggerCondition triggerCondition) {
		Trigger trigger = findBuffTrigger(buff, triggerCondition);
		if (trigger == null)
			return false;
		if (!checkRestrictions(buff, player))
			return false;
		return trigger.getChance() == 100 || Rnd.chance() < trigger.getChance();
	}

	private Buff.Trigger findBuffTrigger(Buff buff, Buff.TriggerCondition triggerCondition) {
		for (Buff.Trigger trigger : buff.getTriggers()) {
			if (trigger.getCondition() == triggerCondition)
				return trigger;
		}
		return null;
	}

	private boolean checkRestrictions(Buff buff, Player player) {
		if (!isAllowedToday(buff, player))
			return false;
		if (!isAllowedOnCurrentMap(buff, player))
			return false;
		if (!isAllowedTeamSize(buff, player))
			return false;
		return true;
	}

	private boolean isAllowedTeamSize(Buff buff, Player player) {
		if (buff.getRestriction() == null || buff.getRestriction().getTeamSizeMaxPercent() == 0)
			return true;
		TemporaryPlayerTeam<?> team = player.getCurrentTeam();
		if (team != null) {
			int maxAllowedTeamSize = DataManager.INSTANCE_COOLTIME_DATA.getMaxMemberCount(player.getWorldId(), player.getRace());
			if (maxAllowedTeamSize == 0)
				maxAllowedTeamSize = team.getMaxMemberCount();

			if (buff.getRestriction().getTeamSizeMaxPercent() >= team.size() * 100f / maxAllowedTeamSize)
				return true;
		}
		return false;
	}

	private boolean isAllowedToday(Buff buff, Player player) {
		Set<Integer> allowedBuffDays = this.allowedBuffDays.get(buff);
		return allowedBuffDays == null || allowedBuffDays.contains(dayOfMonth);
	}

	private boolean isAllowedOnCurrentMap(Buff buff, Player player) {
		if (buff.getRestriction() == null || buff.getRestriction().getMaps() == null)
			return true;
		WorldMapInstance worldMapInstance = player.getPosition().getWorldMapInstance();
		for (BuffMapType buffMapType : buff.getRestriction().getMaps()) {
			if (buffMapType.matches(worldMapInstance)) {
				if (buffMapType == BuffMapType.WORLD_MAP)
					return true;
				if (checkInstanceLevel(player))
					return true;
			}
		}
		return false;
	}

	private boolean checkInstanceLevel(Player player) {
		// only allow if the player level is not too high (max 9 levels above the instance entry level)
		InstanceCooltime template = DataManager.INSTANCE_COOLTIME_DATA.getInstanceCooltimeByWorldId(player.getWorldId());
		if (template != null) {
			int instanceLevel = player.getRace() == Race.ELYOS ? template.getEnterMinLevelLight() : template.getEnterMinLevelDark();
			return instanceLevel == 0 || player.getLevel() - instanceLevel < 10;
		}
		return true;
	}

}
