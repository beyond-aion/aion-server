package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.chest.ChestTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author Wakizashi
 */
@XmlRootElement(name = "chest_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChestData {

	@XmlElement(name = "chest")
	private List<ChestTemplate> chests;

	/** A map containing all npc templates */
	private TIntObjectHashMap<ChestTemplate> chestData = new TIntObjectHashMap<>();

	/**
	 * Initializes all maps for subsequent use - Don't nullify initial chest list as it will be used during reload
	 * 
	 * @param u
	 * @param parent
	 */
	void afterUnmarshal(Unmarshaller u, Object parent) {
		chestData.clear();

		for (ChestTemplate chest : chests) {
			chestData.put(chest.getNpcId(), chest);
		}
	}

	public int size() {
		return chestData.size();
	}

	/**
	 * @param npcId
	 * @return
	 */
	public ChestTemplate getChestTemplate(int npcId) {
		return chestData.get(npcId);
	}

	/**
	 * @return the chests
	 */
	public List<ChestTemplate> getChests() {
		return chests;
	}

	/**
	 * @param chests
	 *          the chests to set
	 */
	public void setChests(List<ChestTemplate> chests) {
		this.chests = chests;
		afterUnmarshal(null, null);
	}
}
