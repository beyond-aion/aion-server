package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

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
	private TIntObjectHashMap<InstanceBonusAttr> templates = new TIntObjectHashMap<InstanceBonusAttr>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (InstanceBonusAttr template : instanceBonusattr) {
			templates.put(template.getBuffId(), template);
		}
		instanceBonusattr.clear();
		instanceBonusattr = null;
	}

	public int size() {
		return templates.size();
	}

	public InstanceBonusAttr getInstanceBonusattr(int buffId) {
		return templates.get(buffId);
	}

}
