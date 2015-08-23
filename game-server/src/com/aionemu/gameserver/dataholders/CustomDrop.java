package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.drop.NpcDrop;

/**
 * @author ViAl
 */
@XmlRootElement(name = "custom_drop")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomDrop {
	
	private static final Logger log = LoggerFactory.getLogger(CustomDrop.class);
	
	@XmlElement(name = "npc_drop")
	private List<NpcDrop> npcDrop;
	@XmlTransient
	private Map<Integer, NpcDrop> dropById = new HashMap<Integer, NpcDrop>();

	public NpcDrop getNpcDrop(int npcId) {
		return dropById.get(npcId);
	}
	
	void afterUnmarshal(Unmarshaller u, Object parent) {
		if(npcDrop == null)
			return;
		for(NpcDrop drop : npcDrop) {
			if(dropById.containsKey(drop.getNpcId())) {
				log.warn("Trying to set custom drop for npc "+drop.getNpcId()+" twice!");
			}
			else {
				dropById.put(drop.getNpcId(), drop);
			}
		}
		npcDrop.clear();
		npcDrop = null;
	}

	public int size() {
		return dropById.size();
	}
	
	public Set<Integer> getNpcIds() {
		return dropById.keySet();
	}
}
