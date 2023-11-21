package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.instance_bonusatrr.InstanceBonusAttr;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "instanceBonusattr" })
@XmlRootElement(name = "instance_bonusattrs")
public class InstanceBuffData {

	@XmlElement(name = "instance_bonusattr")
	protected List<InstanceBonusAttr> instanceBonusattr;
	@XmlTransient
	private final Map<Integer, InstanceBonusAttr> templates = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (InstanceBonusAttr template : instanceBonusattr) {
			templates.put(template.getBuffId(), template);
		}
		instanceBonusattr = null;
	}

	public int size() {
		return templates.size();
	}

	public InstanceBonusAttr getInstanceBonusattr(int buffId) {
		return templates.get(buffId);
	}

}
