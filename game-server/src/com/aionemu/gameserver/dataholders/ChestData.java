package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.chest.ChestTemplate;

/**
 * @author Wakizashi
 */
@XmlRootElement(name = "chest_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChestData {

	@XmlElement(name = "chest")
	private List<ChestTemplate> chests;

	@XmlTransient
	private final Map<Integer, ChestTemplate> chestData = new HashMap<>();

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
		chests = null;
	}

	public int size() {
		return chestData.size();
	}

	public ChestTemplate getChestTemplate(int npcId) {
		return chestData.get(npcId);
	}
}
