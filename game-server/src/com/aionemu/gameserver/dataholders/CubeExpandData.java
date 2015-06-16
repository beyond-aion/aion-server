package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.CubeExpandTemplate;

/**
 * This is for the Cube Expanders.
 * 
 * @author dragoon112
 */
@XmlRootElement(name = "cube_expander")
@XmlAccessorType(XmlAccessType.FIELD)
public class CubeExpandData {

	@XmlElement(name = "cube_npc")
	private List<CubeExpandTemplate> clist;
	private TIntObjectHashMap<CubeExpandTemplate> npctlistData = new TIntObjectHashMap<CubeExpandTemplate>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (CubeExpandTemplate npc : clist) {
			npctlistData.put(npc.getNpcId(), npc);
		}
	}

	public int size() {
		return npctlistData.size();
	}

	public CubeExpandTemplate getCubeExpandListTemplate(int id) {
		return npctlistData.get(id);
	}
}
