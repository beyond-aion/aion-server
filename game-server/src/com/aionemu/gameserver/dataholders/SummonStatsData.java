package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.LoggerFactory;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.templates.stats.SummonStatsTemplate;

/**
 * @author ATracer
 */
@XmlRootElement(name = "summon_stats_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class SummonStatsData {

	@XmlElement(name = "summon_stats", required = true)
	private List<SummonStatsType> summonTemplatesList = new FastTable<SummonStatsType>();

	private final TIntObjectHashMap<SummonStatsTemplate> summonTemplates = new TIntObjectHashMap<SummonStatsTemplate>();

	/**
	 * @param u
	 * @param parent
	 */
	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (SummonStatsType st : summonTemplatesList) {
			int code1 = makeHash(st.getNpcIdDark(), st.getRequiredLevel());
			summonTemplates.put(code1, st.getTemplate());
			int code2 = makeHash(st.getNpcIdLight(), st.getRequiredLevel());
			summonTemplates.put(code2, st.getTemplate());
		}
	}

	/**
	 * @param npcId
	 * @param level
	 * @return
	 */
	public SummonStatsTemplate getSummonTemplate(int npcId, int level) {
		SummonStatsTemplate template = summonTemplates.get(makeHash(npcId, level));
		if (template == null) {
			LoggerFactory.getLogger(SummonStatsData.class).warn("Missing summon stats template for npc: " + npcId + ", level: " + level);
			template = summonTemplates.get(level >= 50 ? makeHash(833359, 50) : makeHash(833347, 20)); // TEMP till all templates are done
		}
		return template;
	}

	/**
	 * Size of summon templates
	 * 
	 * @return
	 */
	public int size() {
		return summonTemplates.size();
	}

	@XmlRootElement(name = "summonStatsTemplateType")
	private static class SummonStatsType {

		@XmlAttribute(name = "npc_id_dark", required = true)
		private int npcIdDark;
		@XmlAttribute(name = "npc_id_light", required = true)
		private int npcIdLight;
		@XmlAttribute(name = "level", required = true)
		private int requiredLevel;

		@XmlElement(name = "stats_template")
		private SummonStatsTemplate template;

		public int getNpcIdDark() {
			return npcIdDark;
		}

		public int getNpcIdLight() {
			return npcIdLight;
		}

		public int getRequiredLevel() {
			return requiredLevel;
		}

		public SummonStatsTemplate getTemplate() {
			return template;
		}
	}

	/**
	 * Note:<br>
	 * max level is 255
	 * 
	 * @param npcId
	 * @param level
	 * @return
	 */
	private static int makeHash(int npcId, int level) {
		return npcId << 8 | level;
	}
}
