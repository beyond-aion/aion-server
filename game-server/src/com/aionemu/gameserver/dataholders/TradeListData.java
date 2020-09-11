package com.aionemu.gameserver.dataholders;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * This is a container holding and serving all {@link NpcTemplate} instances.<br>
 * Briefly: Every {@link Npc} instance represents some class of NPCs among which each have the same id, name, items, statistics. Data for such NPC
 * class is defined in {@link NpcTemplate} and is uniquely identified by npc id.
 * 
 * @author Luno
 */
@XmlRootElement(name = "npc_trade_list")
@XmlAccessorType(XmlAccessType.FIELD)
public class TradeListData {

	@XmlElement(name = "tradelist_template")
	private List<TradeListTemplate> tlist;

	@XmlElement(name = "trade_in_list_template")
	private List<TradeListTemplate> tInlist;

	@XmlElement(name = "purchase_template")
	private List<TradeListTemplate> plist;

	/** A map containing all trade list templates */
	private TIntObjectHashMap<TradeListTemplate> npctlistData = new TIntObjectHashMap<>();

	private TIntObjectHashMap<TradeListTemplate> npcTradeInlistData = new TIntObjectHashMap<>();

	private TIntObjectHashMap<TradeListTemplate> npcPurchaseTemplateData = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (TradeListTemplate npc : tlist) {
			npctlistData.put(npc.getNpcId(), npc);
		}

		for (TradeListTemplate npc : tInlist) {
			npcTradeInlistData.put(npc.getNpcId(), npc);
		}
		for (TradeListTemplate npc : plist) {
			npcPurchaseTemplateData.put(npc.getNpcId(), npc);
		}
	}

	public int size() {
		return npctlistData.size();
	}

	/**
	 * Returns an {@link TradeListTemplate} object with given id.
	 * 
	 * @param id
	 *          id of NPC
	 * @return TradeListTemplate object containing data about NPC with that id.
	 */
	public TradeListTemplate getTradeListTemplate(int id) {
		return npctlistData.get(id);
	}

	public TradeListTemplate getTradeInListTemplate(int id) {
		return npcTradeInlistData.get(id);
	}

	public TradeListTemplate getPurchaseTemplate(int id) {
		return npcPurchaseTemplateData.get(id);
	}

	/**
	 * @return id of NPC.
	 */
	public TIntObjectHashMap<TradeListTemplate> getTradeListTemplate() {
		return npctlistData;
	}

	public void validateBuyLists(Collection<NpcTemplate> npcTemplates) {
		List<Integer> missingNpcIds = npcTemplates.stream()
				.filter(npc -> npc.supportsAction(DialogAction.BUY) && getTradeListTemplate(npc.getTemplateId()) == null)
				.map(NpcTemplate::getTemplateId).sorted()
				.collect(Collectors.toList());
		if (!missingNpcIds.isEmpty())
			LoggerFactory.getLogger(getClass()).warn("Missing trade lists for these npcs: " + missingNpcIds);
	}
}
