package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.templates.stats.CalculatedPlayerStatsTemplate;
import com.aionemu.gameserver.model.templates.stats.PlayerStatsTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastTable;

/**
 * Created on: 31.07.2009 14:20:03
 * 
 * @author Aquanox
 */
@XmlRootElement(name = "player_stats_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlayerStatsData {

	@XmlElement(name = "player_stats", required = true)
	private List<PlayerStatsType> templatesList = new FastTable<PlayerStatsType>();

	private final TIntObjectHashMap<PlayerStatsTemplate> playerTemplates = new TIntObjectHashMap<PlayerStatsTemplate>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (PlayerStatsType pt : templatesList) {
			int code = makeHash(pt.getRequiredPlayerClass(), pt.getRequiredLevel());
			PlayerStatsTemplate template = pt.getTemplate();
			// XXX template values for hp/mp are the effective values from retail, including health and will modifiers so we need to remove these here
			// ceil since these values get floored in stat calculation, where health and will are applied as the base rate
			template.setMaxMp((int) Math.ceil(template.getMaxMp() * 100f / template.getWill()));
			template.setMaxHp((int) Math.ceil(template.getMaxHp() * 100f / template.getHealth()));
			playerTemplates.put(code, pt.getTemplate());
		}

		/** for unknown templates **/
		playerTemplates.put(makeHash(PlayerClass.WARRIOR, 0), new CalculatedPlayerStatsTemplate(PlayerClass.WARRIOR));
		playerTemplates.put(makeHash(PlayerClass.ASSASSIN, 0), new CalculatedPlayerStatsTemplate(PlayerClass.ASSASSIN));
		playerTemplates.put(makeHash(PlayerClass.CHANTER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.CHANTER));
		playerTemplates.put(makeHash(PlayerClass.CLERIC, 0), new CalculatedPlayerStatsTemplate(PlayerClass.CLERIC));
		playerTemplates.put(makeHash(PlayerClass.GLADIATOR, 0), new CalculatedPlayerStatsTemplate(PlayerClass.GLADIATOR));
		playerTemplates.put(makeHash(PlayerClass.MAGE, 0), new CalculatedPlayerStatsTemplate(PlayerClass.MAGE));
		playerTemplates.put(makeHash(PlayerClass.PRIEST, 0), new CalculatedPlayerStatsTemplate(PlayerClass.PRIEST));
		playerTemplates.put(makeHash(PlayerClass.RANGER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.RANGER));
		playerTemplates.put(makeHash(PlayerClass.SCOUT, 0), new CalculatedPlayerStatsTemplate(PlayerClass.SCOUT));
		playerTemplates.put(makeHash(PlayerClass.SORCERER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.SORCERER));
		playerTemplates.put(makeHash(PlayerClass.SPIRIT_MASTER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.SPIRIT_MASTER));
		playerTemplates.put(makeHash(PlayerClass.TEMPLAR, 0), new CalculatedPlayerStatsTemplate(PlayerClass.TEMPLAR));
		playerTemplates.put(makeHash(PlayerClass.RIDER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.RIDER));
		playerTemplates.put(makeHash(PlayerClass.ARTIST, 0), new CalculatedPlayerStatsTemplate(PlayerClass.ARTIST));
		playerTemplates.put(makeHash(PlayerClass.ENGINEER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.ENGINEER));
		playerTemplates.put(makeHash(PlayerClass.BARD, 0), new CalculatedPlayerStatsTemplate(PlayerClass.BARD));
		playerTemplates.put(makeHash(PlayerClass.GUNNER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.GUNNER));

		templatesList.clear();
		templatesList = null;
	}

	/**
	 * @param playerClass
	 * @param level
	 * @return
	 */
	public PlayerStatsTemplate getTemplate(PlayerClass playerClass, int level) {
		PlayerStatsTemplate template = playerTemplates.get(makeHash(playerClass, level));
		if (template == null)
			template = getTemplate(playerClass, 0);
		return template;
	}

	/**
	 * Size of player templates
	 * 
	 * @return
	 */
	public int size() {
		return playerTemplates.size();
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

	/**
	 * @param playerClass
	 * @param level
	 * @return
	 */
	private static int makeHash(PlayerClass playerClass, int level) {
		return level << 8 | playerClass.ordinal();
	}
}
