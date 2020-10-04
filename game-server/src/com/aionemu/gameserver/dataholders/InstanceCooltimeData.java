package com.aionemu.gameserver.dataholders;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceCoolTimeType;
import com.aionemu.gameserver.model.templates.InstanceCooltime;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author VladimirZ
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "instance_cooltimes")
public class InstanceCooltimeData {

	@XmlElement(name = "instance_cooltime", required = true)
	protected List<InstanceCooltime> instanceCooltime;

	private Map<Integer, InstanceCooltime> instanceCooltimes = new LinkedHashMap<>();
	private Map<Integer, Integer> syncIdToMapId = new HashMap<>();

	/**
	 * @param u
	 * @param parent
	 */
	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (InstanceCooltime tmp : instanceCooltime) {
			instanceCooltimes.put(tmp.getWorldId(), tmp);
			syncIdToMapId.put(tmp.getSyncId(), tmp.getWorldId());
		}
		instanceCooltime.clear();
	}

	public Map<Integer, InstanceCooltime> getInstanceCooltimes() {
		return new LinkedHashMap<>(instanceCooltimes);
	}

	public InstanceCooltime getInstanceCooltimeByWorldId(int worldId) {
		return instanceCooltimes.get(worldId);
	}

	public int getInstanceMaxCountByWorldId(int worldId) {
		return instanceCooltimes.get(worldId).getMaxCount();
	}

	public int getMaxMemberCount(int worldId, Race race) {
		InstanceCooltime template = getInstanceCooltimeByWorldId(worldId);
		return template == null ? 0 : race == Race.ELYOS ? template.getMaxMemberLight() : template.getMaxMemberDark();
	}

	public int getWorldId(int syncId) {
		return syncIdToMapId.getOrDefault(syncId, 0);
	}

	public long calculateInstanceEntranceCooltime(Player player, int worldId) {
		int instanceCooldownRate = InstanceService.getInstanceRate(player, worldId);
		long instanceCoolTime = 0;
		InstanceCooltime clt = getInstanceCooltimeByWorldId(worldId);
		if (clt == null || clt.getMaxCount() == 0)
			return 0;
		switch (clt.getCoolTimeType()) {
			case DAILY:
			case WEEKLY:
				int hour = clt.getEntCoolTime() / 100;
				int minute = clt.getEntCoolTime() % 100;
				ZonedDateTime now = ServerTime.now();
				ZonedDateTime repeatDate = now.with(LocalTime.of(hour, minute));
				if (now.isAfter(repeatDate))
					repeatDate = repeatDate.plusDays(1);
				if (clt.getCoolTimeType() == InstanceCoolTimeType.WEEKLY)
					repeatDate = repeatDate.plusDays(calculateDaysUntilReset(clt, repeatDate.getDayOfWeek()));
				instanceCoolTime = repeatDate.toEpochSecond() * 1000;
				break;
			case RELATIVE:
				int minutes = clt.getEntCoolTime();
				if (minutes == 0) // unlimited entrance, no need to store
					return 0;
				instanceCoolTime = System.currentTimeMillis() + (minutes * 60 * 1000);
				break;
			default:
				LoggerFactory.getLogger(this.getClass()).warn("Unhandled InstanceCoolTimeType: " + clt.getCoolTimeType());
		}
		if (instanceCooldownRate != 1)
			instanceCoolTime = System.currentTimeMillis() + ((instanceCoolTime - System.currentTimeMillis()) / instanceCooldownRate);
		return instanceCoolTime;
	}

	private int calculateDaysUntilReset(InstanceCooltime clt, DayOfWeek day) {
		List<Integer> resetDaysSorted = Arrays.stream(clt.getTypeValue().split(",")).map(dayStr -> getDay(dayStr)).sorted().collect(Collectors.toList());
		for (Integer resetDay : resetDaysSorted) {
			if (resetDay >= day.getValue())
				return resetDay - day.getValue();
		}
		return (7 - day.getValue()) + resetDaysSorted.get(0);
	}

	private int getDay(String day) {
		if (day.equals("Mon")) {
			return 1;
		} else if (day.equals("Tue")) {
			return 2;
		} else if (day.equals("Wed")) {
			return 3;
		} else if (day.equals("Thu")) {
			return 4;
		} else if (day.equals("Fri")) {
			return 5;
		} else if (day.equals("Sat")) {
			return 6;
		} else if (day.equals("Sun")) {
			return 7;
		}
		throw new IllegalArgumentException("Invalid Day: " + day);
	}

	public Integer size() {
		return instanceCooltimes.size();
	}
}
