package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.drop.NpcDrop;

/**
 * @author ViAl, Neon
 */
@XmlRootElement(name = "custom_drop")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomDrop {

	@XmlElement(name = "npc_drop")
	private List<NpcDrop> npcDrop;
	@XmlTransient
	private Map<Integer, NpcDrop> dropById = new HashMap<>();

	public NpcDrop getNpcDrop(int npcId) {
		return dropById.get(npcId);
	}

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (NpcDrop drop : npcDrop) {
			if (dropById.putIfAbsent(drop.getNpcId(), drop) != null)
				LoggerFactory.getLogger(CustomDrop.class).warn("Tried to set custom drop for npc " + drop.getNpcId() + " twice!");
		}
		npcDrop = null;
	}

	public int size() {
		return dropById.size();
	}
}
