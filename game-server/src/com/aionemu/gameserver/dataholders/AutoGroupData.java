package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.autogroup.AutoGroup;

import gnu.trove.map.hash.TIntObjectHashMap;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "autoGroup" })
@XmlRootElement(name = "auto_groups")
public class AutoGroupData {

	@XmlElement(name = "auto_group")
	protected List<AutoGroup> autoGroup;
	@XmlTransient
	private TIntObjectHashMap<AutoGroup> autoGroupByInstanceId = new TIntObjectHashMap<>();
	@XmlTransient
	private TIntObjectHashMap<AutoGroup> autoGroupByNpcId = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		for (AutoGroup ag : autoGroup) {
			autoGroupByInstanceId.put(ag.getId(), ag);

			if (!ag.getNpcIds().isEmpty()) {
				for (int npcId : ag.getNpcIds()) {
					autoGroupByNpcId.put(npcId, ag);
				}
			}
		}
		autoGroup.clear();
		autoGroup = null;
	}

	public AutoGroup getTemplateByInstaceMaskId(int maskId) {
		return autoGroupByInstanceId.get(maskId);
	}

	public int size() {
		return autoGroupByInstanceId.size();
	}
}
