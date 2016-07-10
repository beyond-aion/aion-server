package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.NpcStatCalculation;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.npc.NpcRank;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * This is a container holding and serving all {@link NpcTemplate} instances.<br>
 * Briefly: Every {@link Npc} instance represents some class of NPCs among which each have the same id, name, items, statistics. Data for such NPC
 * class is defined in {@link NpcTemplate} and is uniquely identified by npc id.
 * 
 * @author Luno
 */
@XmlRootElement(name = "npc_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class NpcData {

	@XmlElement(name = "npc_template")
	private List<NpcTemplate> npcs;

	/** A map containing all npc templates */
	@XmlTransient
	private TIntObjectHashMap<NpcTemplate> npcData = new TIntObjectHashMap<NpcTemplate>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (NpcTemplate npc : npcs) {
			npcData.put(npc.getTemplateId(), npc);
			if (npc.getTribe() != null && !npc.getTribe().isUsed())
				npc.getTribe().setUsed(true);
			if (npc.getFuncDialogIds() != null) {
				for (Integer dialogId : npc.getFuncDialogIds()) {
					if (DialogAction.getByActionId(dialogId) == DialogAction.NULL && dialogId != DialogAction.NULL.id())
						LoggerFactory.getLogger(NpcData.class).warn("Unknown dialog action " + dialogId + " for Npc " + npc.getTemplateId());
				}
			}
			NpcRating rating = npc.getRating();
			NpcRank rank = npc.getRank();
			int level = npc.getLevel();
			if (npc.getStatsTemplate().getMaxHp() >= 0)
				npc.getStatsTemplate().setMaxXp(NpcStatCalculation.calculateExp(rating, rank, npc.getStatsTemplate().getMaxHp()));
			if (npc.getStatsTemplate().getAttack() == 0)
				npc.getStatsTemplate().setAttack(NpcStatCalculation.calculateStat(StatEnum.PHYSICAL_ATTACK, rating, rank, level));
			if (npc.getStatsTemplate().getAccuracy() == 0)
				npc.getStatsTemplate().setAccuracy(NpcStatCalculation.calculateStat(StatEnum.PHYSICAL_ACCURACY, rating, rank, level));
			if (npc.getStatsTemplate().getMagicalAttack() == 0)
				npc.getStatsTemplate().setMagicalAttack(NpcStatCalculation.calculateStat(StatEnum.MAGICAL_ATTACK, rating, rank, level));
			if (npc.getStatsTemplate().getMacc() == 0)
				npc.getStatsTemplate().setMacc(NpcStatCalculation.calculateStat(StatEnum.MAGICAL_ACCURACY, rating, rank, level));
			if (npc.getStatsTemplate().getMresist() == 0)
				npc.getStatsTemplate().setMresist(NpcStatCalculation.calculateStat(StatEnum.MAGICAL_RESIST, rating, rank, level));
			if (npc.getStatsTemplate().getPdef() == 0)
				npc.getStatsTemplate().setPdef(NpcStatCalculation.calculateStat(StatEnum.PHYSICAL_DEFENSE, rating, rank, level));
			if (npc.getStatsTemplate().getParry() == 0)
				npc.getStatsTemplate().setParry(NpcStatCalculation.calculateStat(StatEnum.PARRY, rating, rank, level));
			if (level >= 60 && npc.getStatsTemplate().getStrikeResist() == 0)
				npc.getStatsTemplate().setStrikeResist(NpcStatCalculation.calculateStat(StatEnum.PHYSICAL_CRITICAL_RESIST, rating, rank, level));
		}
		npcs.clear();
		npcs = null;
	}

	public int size() {
		return npcData.size();
	}

	/**
	 * /** Returns an {@link NpcTemplate} object with given id.
	 * 
	 * @param id
	 *          id of NPC
	 * @return NpcTemplate object containing data about NPC with that id.
	 */
	public NpcTemplate getNpcTemplate(int id) {
		return npcData.get(id);
	}

	/**
	 * @return the npcData
	 */
	public TIntObjectHashMap<NpcTemplate> getNpcData() {
		return npcData;
	}
}
