package com.aionemu.gameserver.model.templates.event;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Neon
 */
@XmlType(name = "Buff")
@XmlAccessorType(XmlAccessType.FIELD)
public class Buff {

	@XmlAttribute(name = "skill_ids", required = true)
	private Set<Integer> skillIds;
	@XmlAttribute(name = "pool")
	private int pool;
	@XmlAttribute(name = "permanent")
	private boolean isPermanent;
	@XmlAttribute(name = "team")
	private boolean isTeam;
	@XmlElement(name = "restriction")
	private BuffRestriction restriction;
	@XmlElement(name = "trigger", required = true)
	private List<Trigger> triggers;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (pool > skillIds.size())
			LoggerFactory.getLogger(Buff.class).warn("Pool size for event buffs must be smaller than skill id size (skill ids: " + skillIds + ").");
	}

	public Set<Integer> getSkillIds() {
		return skillIds;
	}

	public List<Trigger> getTriggers() {
		return triggers;
	}

	public int getPool() {
		return pool;
	}

	public boolean isPermanent() {
		return isPermanent;
	}

	public boolean isTeam() {
		return isTeam;
	}

	public BuffRestriction getRestriction() {
		return restriction;
	}

	@XmlType(name = "BuffMapType")
	@XmlEnum
	public enum BuffMapType {
		WORLD_MAP(instance -> !instance.getTemplate().isInstance()),
		SOLO_INSTANCE(instance -> instance.getMaxPlayers() == 1),
		GROUP_INSTANCE(instance -> instance.getMaxPlayers() > 1 && instance.getMaxPlayers() <= 6),
		ALLIANCE_INSTANCE(instance -> instance.getMaxPlayers() > 6 && instance.getMaxPlayers() <= 24);

		private final Predicate<WorldMapInstance> matches;

		BuffMapType(Predicate<WorldMapInstance> matches) {
			this.matches = matches;
		}

		public boolean matches(WorldMapInstance worldMapInstance) {
			return matches.test(worldMapInstance);
		}
	}

	@XmlType(name = "Trigger")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Trigger {

		@XmlAttribute
		private TriggerCondition condition;
		@XmlAttribute
		private float chance = 100;

		public TriggerCondition getCondition() {
			return condition;
		}

		public float getChance() {
			return chance;
		}
	}

	@XmlType(name = "TriggerCondition")
	@XmlEnum
	public enum TriggerCondition {
		ENTER_MAP,
		ENTER_TEAM,
		PVE_KILL,
		PVP_KILL
	}
}
