package com.aionemu.gameserver.dataholders;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.templates.stats.PlayerStatsTemplate;

/**
 * Created on: 31.07.2009 14:20:03
 * 
 * @author Aquanox
 */
@XmlRootElement(name = "player_stats_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlayerStatsData {

	@XmlElement(name = "player_stats", required = true)
	private List<PlayerStatsType> templates;

	@XmlTransient
	private final Map<PlayerClass, Map<Integer, PlayerStatsTemplate>> playerTemplates = new EnumMap<>(PlayerClass.class);

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (PlayerStatsType pt : templates) {
			PlayerStatsTemplate template = pt.getTemplate();
			// XXX template values for hp/mp are the effective values from retail, including health and will modifiers so we need to remove these here
			// ceil since these values get floored in stat calculation, where health and will are applied as the base rate
			template.setMaxMp((int) Math.ceil(template.getMaxMp() * 100f / template.getWill()));
			template.setMaxHp((int) Math.ceil(template.getMaxHp() * 100f / template.getHealth()));
			playerTemplates.compute(pt.getRequiredPlayerClass(), (key, map) -> {
				if (map == null) {
					map = new LinkedHashMap<>();
				}
				if (map.put(pt.getRequiredLevel(), pt.getTemplate()) != null) {
					throw new Error("Duplicate stat template for " + key + ", level: " + pt.getRequiredLevel());
				}
				return map;
			});
		}

		for (int expectedLevel = 1; expectedLevel <= GSConfig.PLAYER_MAX_LEVEL; expectedLevel++) {
			for (Map.Entry<PlayerClass, Map<Integer, PlayerStatsTemplate>> e : playerTemplates.entrySet()) {
				PlayerClass playerClass = e.getKey();
				if (playerClass.isStartingClass()) {
					if (expectedLevel > 9)
						continue;
				} else if (expectedLevel < 9) {
					continue;
				}
				if (e.getValue().get(expectedLevel) == null)
					throw new Error("Missing stat template for " + playerClass + " level " + expectedLevel);
			}
		}
		templates = null;
	}

	public PlayerStatsTemplate getTemplate(PlayerClass playerClass, int level) {
		PlayerStatsTemplate template = playerTemplates.get(playerClass).get(level);
		if (template == null) {
			template = getFallbackTemplate(playerClass, level);
			if (template == null) {
				LoggerFactory.getLogger(PlayerStatsData.class).error("Missing stats template for " + playerClass + ", level: " + level);
				template = getNearestTemplateForLevel(playerClass, level);
			}
		}
		return template;
	}

	private PlayerStatsTemplate getFallbackTemplate(PlayerClass playerClass, int level) {
		if (level < 9) // player was set to low level via command, return the corresponding starting class template
			return playerTemplates.get(playerClass.getStartingClass()).get(level);
		else if (level > 9 && playerClass.isStartingClass()) // player changed class via command while being high level
			return playerTemplates.get(playerClass).get(9);
		else if (level > GSConfig.PLAYER_MAX_LEVEL)
			return playerTemplates.get(playerClass).get(GSConfig.PLAYER_MAX_LEVEL);
		return null;
	}

	private PlayerStatsTemplate getNearestTemplateForLevel(PlayerClass playerClass, int level) {
		Set<Map.Entry<Integer, PlayerStatsTemplate>> templates = playerTemplates.get(playerClass).entrySet();
		// here we search all templates for this playerClass and return the one nearest to the players level
		return templates.stream().min(Comparator.comparing(e -> Math.abs(level - e.getKey()))).map(Map.Entry::getValue).get();
	}

	public int size() {
		return playerTemplates.values().stream().mapToInt(Map::size).sum();
	}

	@XmlRootElement(name = "playerStatsTemplateType")
	private static class PlayerStatsType {

		@XmlAttribute(name = "class", required = true)
		private PlayerClass requiredPlayerClass;
		@XmlAttribute(name = "level", required = true)
		private int requiredLevel;

		@XmlElement(name = "stats_template")
		private PlayerStatsTemplate template;

		public PlayerClass getRequiredPlayerClass() {
			return requiredPlayerClass;
		}

		public int getRequiredLevel() {
			return requiredLevel;
		}

		public PlayerStatsTemplate getTemplate() {
			return template;
		}
	}
}
