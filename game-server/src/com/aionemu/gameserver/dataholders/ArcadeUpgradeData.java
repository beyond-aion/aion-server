package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.arcadeupgrade.ArcadeTab;
import com.aionemu.gameserver.model.templates.arcadeupgrade.ArcadeTabItemList;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author ginho1
 */
@XmlRootElement(name = "arcadelist")
@XmlAccessorType(XmlAccessType.FIELD)
public class ArcadeUpgradeData {

	@XmlElement(name = "tab")
	private List<ArcadeTab> arcadeTabTemplate;
	private TIntObjectHashMap<List<ArcadeTabItemList>> arcadeItemList = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		arcadeItemList.clear();
		for (ArcadeTab template : arcadeTabTemplate)
			arcadeItemList.put(template.getId(), template.getArcadeTabItems());
	}

	public int size() {
		return arcadeItemList.size();
	}

	public List<ArcadeTabItemList> getArcadeTabById(int id) {
		return arcadeItemList.get(id);
	}

	public List<ArcadeTab> getArcadeTabs() {
		return arcadeTabTemplate;
	}
}
