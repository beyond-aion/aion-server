package com.aionemu.gameserver.dataholders;

import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.autogroup.AutoGroup;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "autoGroup" })
@XmlRootElement(name = "auto_groups")
public class AutoGroupData {

	@XmlElement(name = "auto_group")
	protected List<AutoGroup> autoGroup;

	@XmlTransient
	private final Map<Integer, AutoGroup> autoGroupByInstanceId = new HashMap<>();
	@XmlTransient
	private final Map<Integer, List<Integer>> recruitableInstanceMaskIdByPortalNpc = new HashMap<>();

	void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		for (AutoGroup ag : autoGroup) {
			autoGroupByInstanceId.put(ag.getMaskId(), ag);
			if (ag.isRecruitableInstance()) {
				for (int npcId : ag.getNpcIds())
					recruitableInstanceMaskIdByPortalNpc.computeIfAbsent(npcId, k -> new ArrayList<>()).add(ag.getMaskId());
			}
		}
		autoGroup = null;
	}

	public AutoGroup getTemplateByInstanceMaskId(int maskId) {
		return autoGroupByInstanceId.get(maskId);
	}

	public List<Integer> getRecruitableInstanceMaskIds(int portalNpcId) {
		return recruitableInstanceMaskIdByPortalNpc.get(portalNpcId);
	}

	public List<Integer> getRecruitableInstanceMaskIds() {
		return recruitableInstanceMaskIdByPortalNpc.values().stream().flatMap(Collection::stream).distinct().toList();
	}

	public int size() {
		return autoGroupByInstanceId.size();
	}
}
